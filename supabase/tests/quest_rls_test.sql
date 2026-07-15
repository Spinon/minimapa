begin;
select plan(17);

insert into auth.users (
  id, aud, role, email, encrypted_password, email_confirmed_at,
  raw_app_meta_data, raw_user_meta_data, created_at, updated_at
)
values
  ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'authenticated', 'authenticated', 'owner@example.test', '', now(), '{}', '{}', now(), now()),
  ('bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', 'authenticated', 'authenticated', 'other@example.test', '', now(), '{}', '{}', now(), now()),
  ('cccccccc-cccc-4ccc-8ccc-cccccccccccc', 'authenticated', 'authenticated', 'third@example.test', '', now(), '{}', '{}', now(), now());

insert into public.quests (
  id, creator_id, module_version_id, definition_version_id, title, description,
  state, version, assignment_strategy, discovery_visibility, discovery_center,
  discovery_radius_m, principal_minor, currency
)
values
  (
    '30000000-0000-4000-8000-000000000001',
    'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
    '10000000-0000-4000-8000-000000000001',
    '20000000-0000-4000-8000-000000000001',
    'Rascunho privado', '', 'DRAFT', 0, 'FIRST_ELIGIBLE_ACCEPTS', 'PRIVATE',
    extensions.st_point(-47.5604, -22.4102)::extensions.geography, 500, 2000, 'BRL'
  ),
  (
    '30000000-0000-4000-8000-000000000002',
    'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
    '10000000-0000-4000-8000-000000000001',
    '20000000-0000-4000-8000-000000000001',
    'Quest publicada', '', 'PUBLISHED', 1, 'FIRST_ELIGIBLE_ACCEPTS', 'AUTHENTICATED',
    extensions.st_point(-47.5578, -22.4090)::extensions.geography, 750, 3500, 'BRL'
  );

insert into public.quest_participants (quest_id, player_id, universal_role)
values
  ('30000000-0000-4000-8000-000000000001', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'REQUESTER'),
  ('30000000-0000-4000-8000-000000000002', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'REQUESTER');

insert into public.quest_local_delivery_details (
  quest_id, origin_exact, destination_exact, origin_label_private,
  destination_label_private, item_category, item_description,
  declared_value_minor, mass_grams, volume_liters, longest_edge_cm
)
values
  (
    '30000000-0000-4000-8000-000000000001',
    extensions.st_point(-47.5604, -22.4102)::extensions.geography,
    extensions.st_point(-47.5512, -22.4054)::extensions.geography,
    'Origem privada A', 'Destino privado A', 'GENERAL', 'Pacote A', 20000, 2000, 10, 30
  ),
  (
    '30000000-0000-4000-8000-000000000002',
    extensions.st_point(-47.5578, -22.4090)::extensions.geography,
    extensions.st_point(-47.5480, -22.4030)::extensions.geography,
    'Origem privada B', 'Destino privado B', 'GENERAL', 'Pacote B', 30000, 3000, 15, 40
  );

insert into public.quest_events (
  quest_id, event_id, sequence, from_state, to_state, actor_id, occurred_at
)
values (
  '30000000-0000-4000-8000-000000000002',
  '40000000-0000-4000-8000-000000000001',
  1, 'DRAFT', 'PUBLISHED', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', now()
);

select has_extension('postgis', 'PostGIS is enabled');
select has_table('public', 'quests', 'Universal quest table exists');
select is(
  (select count(*) from pg_tables where schemaname = 'public' and tablename in (
    'quest_module_versions', 'quest_definitions', 'quests', 'quest_requirements',
    'quest_participants', 'quest_events', 'quest_local_delivery_details', 'quest_service_details'
  ) and not rowsecurity),
  0::bigint,
  'Every exposed domain table has RLS enabled'
);
select is(
  (select count(*) from pg_policies where schemaname = 'public' and (
    coalesce(qual, '') ilike '%user_metadata%' or coalesce(with_check, '') ilike '%user_metadata%'
  )),
  0::bigint,
  'RLS never trusts user-editable metadata'
);

set local role authenticated;
select set_config('request.jwt.claim.sub', 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', true);
select set_config('request.jwt.claim.role', 'authenticated', true);

select is((select count(*) from public.quests), 1::bigint, 'Other user sees only discoverable quest');
select is((select count(*) from public.quest_local_delivery_details), 0::bigint, 'Other user cannot read exact delivery details');
select is((select count(*) from public.quest_events), 0::bigint, 'Other user cannot read private event timeline');
select throws_ok(
  $$insert into public.quests (
      id, creator_id, module_version_id, definition_version_id, title, state,
      assignment_strategy, discovery_visibility
    ) values (
      '30000000-0000-4000-8000-000000000003',
      'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
      '10000000-0000-4000-8000-000000000001',
      '20000000-0000-4000-8000-000000000001',
      'Forjada', 'DRAFT', 'FIRST_ELIGIBLE_ACCEPTS', 'PRIVATE'
    )$$,
  '42501',
  null,
  'Other user cannot forge creator ownership'
);
select throws_ok(
  $$insert into public.quest_participants (quest_id, player_id, universal_role)
    values ('30000000-0000-4000-8000-000000000002', 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', 'ASSIGNEE')$$,
  '42501',
  null,
  'Client cannot assign itself directly'
);
select is((select count(*) from public.quest_discovery), 1::bigint, 'Discovery view exposes only approximate published quest');

reset role;
set local role authenticated;
select set_config('request.jwt.claim.sub', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', true);
select set_config('request.jwt.claim.role', 'authenticated', true);

select is((select count(*) from public.quests), 2::bigint, 'Creator sees own draft and published quest');
select is((select count(*) from public.quest_local_delivery_details), 2::bigint, 'Creator sees own exact details');
select throws_ok(
  $$update public.quests set state = 'PUBLISHED', version = version + 1
    where id = '30000000-0000-4000-8000-000000000001'$$,
  '42501',
  null,
  'Client cannot publish by bypassing QuestEngine'
);
select lives_ok(
  $$insert into public.quests (
      id, creator_id, module_version_id, definition_version_id, title, state,
      assignment_strategy, discovery_visibility
    ) values (
      '30000000-0000-4000-8000-000000000004',
      'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
      '10000000-0000-4000-8000-000000000001',
      '20000000-0000-4000-8000-000000000001',
      'Meu novo rascunho', 'DRAFT', 'FIRST_ELIGIBLE_ACCEPTS', 'PRIVATE'
    )$$,
  'Creator can create its own draft'
);

reset role;
insert into public.quest_participants (quest_id, player_id, universal_role)
values ('30000000-0000-4000-8000-000000000002', 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', 'ASSIGNEE');

set local role authenticated;
select set_config('request.jwt.claim.sub', 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', true);
select set_config('request.jwt.claim.role', 'authenticated', true);
select is((select count(*) from public.quest_local_delivery_details), 1::bigint, 'Assigned participant can read exact details');
select is((select count(*) from public.quest_events), 1::bigint, 'Assigned participant can read event timeline');

reset role;
set local role anon;
select throws_ok(
  $$select count(*) from public.quests$$,
  '42501',
  null,
  'Anonymous role cannot read quests'
);

select * from finish();
rollback;
