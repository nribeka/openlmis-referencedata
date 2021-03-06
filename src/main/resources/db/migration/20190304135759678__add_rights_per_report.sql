-- WHEN COMMITTING OR REVIEWING THIS FILE: Make sure that the timestamp in the file name (that serves as a version) is the latest timestamp, and that no new migration have been added in the meanwhile.
-- Adding migrations out of order may cause this migration to never execute or behave in an unexpected way.
-- Migrations should NOT BE EDITED. Add a new migration to apply changes.

INSERT INTO rights (id, description, name, type) VALUES ('4eaac390-1156-42c9-9fa0-83130c4519d4', NULL, 'REPORTING_RATE_AND_TIMELINESS_REPORT_VIEW', 'REPORTS');
INSERT INTO rights (id, description, name, type) VALUES ('526e5ee5-b07c-4cc7-bc4b-3fa77bde0699', NULL, 'STOCK_STATUS_REPORT_VIEW', 'REPORTS');
INSERT INTO rights (id, description, name, type) VALUES ('afdbf2a3-c099-40fa-93ea-1c18794e3cd4', NULL, 'STOCKOUTS_REPORT_VIEW', 'REPORTS');
INSERT INTO rights (id, description, name, type) VALUES ('b9efd72e-418b-408a-870e-f43b72ca65aa', NULL, 'CONSUMPTION_REPORT_VIEW', 'REPORTS');
INSERT INTO rights (id, description, name, type) VALUES ('bfc78d70-b4d9-4d8b-b000-3a18c6c505fe', NULL, 'ORDERS_REPORT_VIEW', 'REPORTS');
INSERT INTO rights (id, description, name, type) VALUES ('8c62f9be-db3a-43a6-b9e2-7f22abada06d', NULL, 'ADJUSTMENTS_REPORT_VIEW', 'REPORTS');
INSERT INTO rights (id, description, name, type) VALUES ('83e82ad8-07cc-4bd7-a159-2acbefbe9d66', NULL, 'ADMINISTRATIVE_REPORT_VIEW', 'REPORTS');
