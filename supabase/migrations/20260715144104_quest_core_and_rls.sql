create extension if not exists postgis with schema extensions;

create schema if not exists private;
revoke all on schema private from public;
grant usage on schema private to authenticated;

create table public.quest_module_versions (
  id uuid primary key default gen_random_uuid(),
  module_id text not null check (module_id ~ '^[a-z][a-z0-9-]{2,63}$'),
  version_major integer not null check (version_major > 0),
  version_minor integer not null default 0 check (version_minor >= 0),
  core_min_major integer not null check (core_min_major > 0),
  core_max_exclusive_major integer not null check (core_max_exclusive_major > core_min_major),
  capabilities text[] not null default '{}',
  created_at timestamptz not null default now(),
  unique (module_id, version_major, version_minor)
);

create table public.quest_definitions (
  id uuid primary key default gen_random_uuid(),
  definition_id text not null check (definition_id ~ '^[a-z][a-z0-9-]{2,63}$'),
  version_major integer not null check (version_major > 0),
  version_minor integer not null default 0 check (version_minor >= 0),
  module_version_id uuid not null references public.quest_module_versions(id) on delete restrict,
  category_id text not null check (category_id ~ '^[a-z][a-z0-9.-]{2,95}$'),
  creation_schema_id text not null,
  definition_payload jsonb not null default '{}'::jsonb check (jsonb_typeof(definition_payload) = 'object'),
  lifecycle_state text not null default 'DRAFT' check (lifecycle_state in ('DRAFT', 'CANDIDATE', 'ACTIVE_BETA', 'ACTIVE', 'RETIRED')),
  created_at timestamptz not null default now(),
  unique (definition_id, version_major, version_minor),
  unique (id, module_version_id)
);

create table public.quests (
  id uuid primary key default gen_random_uuid(),
  creator_id uuid not null references auth.users(id) on delete restrict,
  module_version_id uuid not null references public.quest_module_versions(id) on delete restrict,
  definition_version_id uuid not null,
  title text not null check (length(title) between 1 and 120),
  description text not null default '' check (length(description) <= 4000),
  state text not null default 'DRAFT' check (state in ('DRAFT', 'PUBLISHED', 'MATCHING', 'ASSIGNED', 'ACTIVE', 'COMPLETED', 'CANCELLED', 'EXPIRED', 'DISPUTED')),
  version bigint not null default 0 check (version >= 0),
  assignment_strategy text not null check (assignment_strategy in ('FIRST_ELIGIBLE_ACCEPTS', 'OWNER_SELECTS_APPLICATION', 'QUOTE_AND_SCHEDULE', 'INVITE_ONLY')),
  discovery_visibility text not null default 'AUTHENTICATED' check (discovery_visibility in ('PRIVATE', 'AUTHENTICATED')),
  discovery_center extensions.geography(point, 4326),
  discovery_radius_m integer check (discovery_radius_m between 100 and 50000),
  execution_window tstzrange,
  principal_minor bigint check (principal_minor >= 0),
  currency char(3) check (currency is null or currency ~ '^[A-Z]{3}$'),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint quests_definition_matches_module
    foreign key (definition_version_id, module_version_id)
    references public.quest_definitions(id, module_version_id) on delete restrict,
  constraint quests_discovery_pair check (
    (discovery_center is null and discovery_radius_m is null)
    or (discovery_center is not null and discovery_radius_m is not null)
  ),
  constraint quests_execution_window_order check (
    execution_window is null or (not isempty(execution_window) and lower(execution_window) < upper(execution_window))
  )
);

create table public.quest_requirements (
  id uuid primary key default gen_random_uuid(),
  quest_id uuid not null references public.quests(id) on delete cascade,
  requirement_type text not null check (requirement_type ~ '^[A-Z][A-Z0-9_]{2,63}$'),
  version_major integer not null check (version_major > 0),
  version_minor integer not null default 0 check (version_minor >= 0),
  attributes jsonb not null default '{}'::jsonb check (jsonb_typeof(attributes) = 'object'),
  created_at timestamptz not null default now()
);

create table public.quest_participants (
  quest_id uuid not null references public.quests(id) on delete cascade,
  player_id uuid not null references auth.users(id) on delete restrict,
  universal_role text not null check (universal_role in ('REQUESTER', 'CANDIDATE', 'ASSIGNEE', 'BENEFICIARY')),
  created_at timestamptz not null default now(),
  primary key (quest_id, player_id, universal_role)
);

create unique index quest_single_requester_idx
  on public.quest_participants (quest_id)
  where universal_role = 'REQUESTER';

create table public.quest_events (
  quest_id uuid not null references public.quests(id) on delete cascade,
  event_id uuid not null,
  sequence bigint not null check (sequence > 0),
  from_state text not null,
  to_state text not null,
  actor_id uuid not null references auth.users(id) on delete restrict,
  reason_code text,
  event_payload jsonb not null default '{}'::jsonb check (jsonb_typeof(event_payload) = 'object'),
  occurred_at timestamptz not null,
  recorded_at timestamptz not null default now(),
  primary key (quest_id, event_id),
  unique (quest_id, sequence)
);

create table public.quest_local_delivery_details (
  quest_id uuid primary key references public.quests(id) on delete cascade,
  origin_exact extensions.geography(point, 4326) not null,
  destination_exact extensions.geography(point, 4326) not null,
  origin_label_private text not null,
  destination_label_private text not null,
  item_category text not null,
  item_description text not null check (length(item_description) between 1 and 1000),
  declared_value_minor bigint not null check (declared_value_minor between 0 and 100000),
  mass_grams integer not null check (mass_grams between 1 and 25000),
  volume_liters numeric(8,2) not null check (volume_liters > 0 and volume_liters <= 120),
  longest_edge_cm numeric(6,2) not null check (longest_edge_cm > 0 and longest_edge_cm <= 100),
  declarations jsonb not null default '{}'::jsonb check (jsonb_typeof(declarations) = 'object'),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.quest_service_details (
  quest_id uuid primary key references public.quests(id) on delete cascade,
  service_definition_code text not null,
  execution_mode text not null check (execution_mode in ('ONSITE', 'REMOTE', 'HYBRID')),
  scope_payload jsonb not null default '{}'::jsonb check (jsonb_typeof(scope_payload) = 'object'),
  access_instructions_private text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index quests_creator_idx on public.quests (creator_id);
create index quests_discovery_state_idx on public.quests (state, discovery_visibility, created_at desc);
create index quests_discovery_geo_idx on public.quests using gist (discovery_center);
create index quest_requirements_quest_idx on public.quest_requirements (quest_id);
create index quest_participants_player_idx on public.quest_participants (player_id, quest_id);
create index quest_events_actor_idx on public.quest_events (actor_id, quest_id);

create or replace function private.is_quest_participant(target_quest_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
  select exists (
    select 1
    from public.quest_participants participant
    where participant.quest_id = target_quest_id
      and participant.player_id = (select auth.uid())
  );
$$;

create or replace function private.is_quest_creator(target_quest_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
  select exists (
    select 1
    from public.quests quest
    where quest.id = target_quest_id
      and quest.creator_id = (select auth.uid())
  );
$$;

create or replace function private.is_quest_creator_draft(target_quest_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
  select exists (
    select 1
    from public.quests quest
    where quest.id = target_quest_id
      and quest.creator_id = (select auth.uid())
      and quest.state = 'DRAFT'
  );
$$;

revoke all on function private.is_quest_participant(uuid) from public;
revoke all on function private.is_quest_creator(uuid) from public;
revoke all on function private.is_quest_creator_draft(uuid) from public;
grant execute on function private.is_quest_participant(uuid) to authenticated;
grant execute on function private.is_quest_creator(uuid) to authenticated;
grant execute on function private.is_quest_creator_draft(uuid) to authenticated;

alter table public.quest_module_versions enable row level security;
alter table public.quest_definitions enable row level security;
alter table public.quests enable row level security;
alter table public.quest_requirements enable row level security;
alter table public.quest_participants enable row level security;
alter table public.quest_events enable row level security;
alter table public.quest_local_delivery_details enable row level security;
alter table public.quest_service_details enable row level security;

revoke all on table public.quest_module_versions from anon, authenticated;
revoke all on table public.quest_definitions from anon, authenticated;
revoke all on table public.quests from anon, authenticated;
revoke all on table public.quest_requirements from anon, authenticated;
revoke all on table public.quest_participants from anon, authenticated;
revoke all on table public.quest_events from anon, authenticated;
revoke all on table public.quest_local_delivery_details from anon, authenticated;
revoke all on table public.quest_service_details from anon, authenticated;

grant select on table public.quest_module_versions to authenticated;
grant select on table public.quest_definitions to authenticated;
grant select, insert, update, delete on table public.quests to authenticated;
grant select, insert, update, delete on table public.quest_requirements to authenticated;
grant select on table public.quest_participants to authenticated;
grant select on table public.quest_events to authenticated;
grant select, insert, update, delete on table public.quest_local_delivery_details to authenticated;
grant select, insert, update, delete on table public.quest_service_details to authenticated;

create policy quest_modules_authenticated_read
on public.quest_module_versions for select
to authenticated
using (true);

create policy quest_definitions_authenticated_read
on public.quest_definitions for select
to authenticated
using (true);

create policy quests_authenticated_read
on public.quests for select
to authenticated
using (
  creator_id = (select auth.uid())
  or private.is_quest_participant(id)
  or (discovery_visibility = 'AUTHENTICATED' and state in ('PUBLISHED', 'MATCHING'))
);

create policy quests_creator_insert_draft
on public.quests for insert
to authenticated
with check (
  creator_id = (select auth.uid())
  and state = 'DRAFT'
  and version = 0
);

create policy quests_creator_update_draft
on public.quests for update
to authenticated
using (creator_id = (select auth.uid()) and state = 'DRAFT')
with check (creator_id = (select auth.uid()) and state = 'DRAFT');

create policy quests_creator_delete_draft
on public.quests for delete
to authenticated
using (creator_id = (select auth.uid()) and state = 'DRAFT');

create policy quest_requirements_visible_quest_read
on public.quest_requirements for select
to authenticated
using (exists (select 1 from public.quests quest where quest.id = quest_id));

create policy quest_requirements_creator_insert_draft
on public.quest_requirements for insert
to authenticated
with check (private.is_quest_creator_draft(quest_id));

create policy quest_requirements_creator_update_draft
on public.quest_requirements for update
to authenticated
using (private.is_quest_creator_draft(quest_id))
with check (private.is_quest_creator_draft(quest_id));

create policy quest_requirements_creator_delete_draft
on public.quest_requirements for delete
to authenticated
using (private.is_quest_creator_draft(quest_id));

create policy quest_participants_party_read
on public.quest_participants for select
to authenticated
using (private.is_quest_participant(quest_id) or private.is_quest_creator(quest_id));

create policy quest_events_party_read
on public.quest_events for select
to authenticated
using (private.is_quest_participant(quest_id) or private.is_quest_creator(quest_id));

create policy delivery_details_party_read
on public.quest_local_delivery_details for select
to authenticated
using (private.is_quest_participant(quest_id) or private.is_quest_creator(quest_id));

create policy delivery_details_creator_insert_draft
on public.quest_local_delivery_details for insert
to authenticated
with check (private.is_quest_creator_draft(quest_id));

create policy delivery_details_creator_update_draft
on public.quest_local_delivery_details for update
to authenticated
using (private.is_quest_creator_draft(quest_id))
with check (private.is_quest_creator_draft(quest_id));

create policy delivery_details_creator_delete_draft
on public.quest_local_delivery_details for delete
to authenticated
using (private.is_quest_creator_draft(quest_id));

create policy service_details_party_read
on public.quest_service_details for select
to authenticated
using (private.is_quest_participant(quest_id) or private.is_quest_creator(quest_id));

create policy service_details_creator_insert_draft
on public.quest_service_details for insert
to authenticated
with check (private.is_quest_creator_draft(quest_id));

create policy service_details_creator_update_draft
on public.quest_service_details for update
to authenticated
using (private.is_quest_creator_draft(quest_id))
with check (private.is_quest_creator_draft(quest_id));

create policy service_details_creator_delete_draft
on public.quest_service_details for delete
to authenticated
using (private.is_quest_creator_draft(quest_id));

create view public.quest_discovery
with (security_invoker = true)
as
select
  quest.id,
  quest.definition_version_id,
  quest.title,
  quest.state,
  quest.assignment_strategy,
  quest.discovery_center,
  quest.discovery_radius_m,
  quest.execution_window,
  quest.principal_minor,
  quest.currency,
  quest.created_at
from public.quests quest
where quest.discovery_visibility = 'AUTHENTICATED'
  and quest.state in ('PUBLISHED', 'MATCHING');

revoke all on table public.quest_discovery from anon, authenticated;
grant select on table public.quest_discovery to authenticated;
