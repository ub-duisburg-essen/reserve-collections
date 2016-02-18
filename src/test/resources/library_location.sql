---
-- #%L
-- Semesterapparate
-- $Id:$
-- $HeadURL:$
-- %%
-- Copyright (C) 2014 Universitaet Duisburg Essen
-- %%
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--      http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- #L%
---
INSERT INTO public.library_location (id, name, parentid, physical) VALUES (0, 'Online', null, null);
INSERT INTO public.library_location (id, name, parentid, physical) VALUES (10, 'Essen', null, true);
INSERT INTO public.library_location (id, name, parentid, physical) VALUES (11, 'Duisburg', null, true);
INSERT INTO public.library_location (id, name, parentid, physical) VALUES (1, 'GW/GSW', 10, true);
INSERT INTO public.library_location (id, name, parentid, physical) VALUES (3, 'MNT', 10, true);
INSERT INTO public.library_location (id, name, parentid, physical) VALUES (5, 'Medizin', 10, true);
INSERT INTO public.library_location (id, name, parentid, physical) VALUES (6, 'LK', 11, true);
INSERT INTO public.library_location (id, name, parentid, physical) VALUES (7, 'BA', 11, true);
INSERT INTO public.library_location (id, name, parentid, physical) VALUES (8, 'MC', 11, true);
INSERT INTO public.library_location (id, name, parentid, physical) VALUES (4, '-', 10, true);