DROP FUNCTION IF EXISTS obsParent;
delimiter //

CREATE FUNCTION `obsParent`(obsid int) RETURNS int(11)
    DETERMINISTIC
BEGIN
    DECLARE parent_id int;
    DECLARE conceptid int;
    sloop:LOOP
        SET parent_id = NULL;
        select obs_group_id into parent_id from obs where obs_id = obsid;
        IF parent_id IS NULL THEN
            LEAVE sloop;
        END IF;
        SET obsid = parent_id;
        ITERATE sloop;
    END LOOP;
    select concept_id into conceptid from obs where obs_id = obsid;
    RETURN conceptid;
END //

delimiter ;