CREATE TABLE wdmc_referrals (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_key VARCHAR(255) UNIQUE NOT NULL,
  referral_key VARCHAR(255) UNIQUE DEFAULT NULL,
  invited_by VARCHAR(255) DEFAULT NULL,
  data JSON NOT NULL,
  FOREIGN KEY (invited_by) REFERENCES wdmc_referrals(user_key)
);
-- INSERT INTO wdmc_referrals (user_key, referral_key, data)
-- VALUES ('272924120142970892', '272924120142970892', '{}' );

-- -- INSERT A CALENDAR DATA
-- UPDATE
--     wdmc_referrals
-- SET
--     data = JSON_INSERT(
--         data,
--         '$.sysName',
--     	'{count:0}'
--     )
-- WHERE
--     user_key = 272924120142970892 OR referral_key = "xxx";

-- -- UPDATE A CALENDAR COUNTS
-- UPDATE
--     wdmc_referrals
-- SET
--     `data` = JSON_SET(
--         `data`,
--         '$.sysName[0].count',
--         CONVERT(
--             COALESCE(JSON_EXTRACT(data, '$.sysName[0].count'), 0),
--             SIGNED
--         ) + 1
--     )
-- WHERE
--     user_key = 272924120142970892 OR referral_key = "xxx";
-- -- GET A SINGLE CALENDAR DATA

-- SELECT
--     JSON_EXTRACT(data, '$.sysName') AS singleData
-- FROM
--     wdmc_referrals
-- WHERE
--     user_key = 272924120142970892 OR referral_key = "xxx";