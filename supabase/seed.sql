-- Provider-neutral catalog data for local development. No personal data is seeded.
insert into public.quest_module_versions (
  id,
  module_id,
  version_major,
  version_minor,
  core_min_major,
  core_max_exclusive_major,
  capabilities
)
values (
  '10000000-0000-4000-8000-000000000001',
  'local-delivery',
  1,
  0,
  1,
  2,
  array['LOCATION', 'ROUTE', 'SCHEDULING', 'EVIDENCE', 'LIVE_TRACKING']
)
on conflict (id) do nothing;

insert into public.quest_definitions (
  id,
  definition_id,
  version_major,
  version_minor,
  module_version_id,
  category_id,
  creation_schema_id,
  definition_payload,
  lifecycle_state
)
values (
  '20000000-0000-4000-8000-000000000001',
  'small-package',
  1,
  0,
  '10000000-0000-4000-8000-000000000001',
  'delivery.small-package',
  'local-delivery.creation',
  '{"maxDeclaredValueMinor": 100000, "maxMassGrams": 25000, "pilotRadiusMeters": 50000}'::jsonb,
  'ACTIVE_BETA'
)
on conflict (id) do nothing;
