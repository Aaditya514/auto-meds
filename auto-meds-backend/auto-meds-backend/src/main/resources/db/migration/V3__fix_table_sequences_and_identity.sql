-- V3: Ensure all entity ID columns have sequences attached for GenerationType.IDENTITY compatibility

CREATE SEQUENCE IF NOT EXISTS seq_cart_items START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_carts START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_users START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_medicines START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_orders START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_order_items START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_prescriptions START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_subscriptions START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_notifications START WITH 1 INCREMENT BY 1;

ALTER TABLE cart_items ALTER COLUMN id SET DEFAULT nextval('seq_cart_items');
ALTER TABLE carts ALTER COLUMN id SET DEFAULT nextval('seq_carts');
ALTER TABLE users ALTER COLUMN id SET DEFAULT nextval('seq_users');
ALTER TABLE medicines ALTER COLUMN id SET DEFAULT nextval('seq_medicines');
ALTER TABLE orders ALTER COLUMN id SET DEFAULT nextval('seq_orders');
ALTER TABLE order_items ALTER COLUMN id SET DEFAULT nextval('seq_order_items');
ALTER TABLE prescriptions ALTER COLUMN id SET DEFAULT nextval('seq_prescriptions');
ALTER TABLE subscriptions ALTER COLUMN id SET DEFAULT nextval('seq_subscriptions');
ALTER TABLE notifications ALTER COLUMN id SET DEFAULT nextval('seq_notifications');

SELECT setval('seq_cart_items', COALESCE((SELECT MAX(id) FROM cart_items), 0) + 1, false);
SELECT setval('seq_carts', COALESCE((SELECT MAX(id) FROM carts), 0) + 1, false);
SELECT setval('seq_users', COALESCE((SELECT MAX(id) FROM users), 0) + 1, false);
SELECT setval('seq_medicines', COALESCE((SELECT MAX(id) FROM medicines), 0) + 1, false);
SELECT setval('seq_orders', COALESCE((SELECT MAX(id) FROM orders), 0) + 1, false);
SELECT setval('seq_order_items', COALESCE((SELECT MAX(id) FROM order_items), 0) + 1, false);
SELECT setval('seq_prescriptions', COALESCE((SELECT MAX(id) FROM prescriptions), 0) + 1, false);
SELECT setval('seq_subscriptions', COALESCE((SELECT MAX(id) FROM subscriptions), 0) + 1, false);
SELECT setval('seq_notifications', COALESCE((SELECT MAX(id) FROM notifications), 0) + 1, false);
