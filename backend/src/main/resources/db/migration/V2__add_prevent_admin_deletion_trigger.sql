CREATE OR REPLACE FUNCTION prevent_admin_deletion()
    RETURNS TRIGGER AS $$
BEGIN
    IF OLD.id = 1 THEN
        RAISE EXCEPTION 'Cannot delete the default admin user';
    END IF;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- Create the trigger
CREATE TRIGGER protect_default_admin
    BEFORE DELETE ON users
    FOR EACH ROW
EXECUTE FUNCTION prevent_admin_deletion();