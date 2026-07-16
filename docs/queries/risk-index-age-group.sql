-- Risk Index Trends by Age Group
-- Visualization query for Grafana

SELECT
    TO_TIMESTAMP(calculation_month || '-01', 'YYYY-MM-DD') AS time,
    MAX(risk_index) FILTER (WHERE segment_value = '18-24') AS "18-24",
    MAX(risk_index) FILTER (WHERE segment_value = '25-34') AS "25-34",
    MAX(risk_index) FILTER (WHERE segment_value = '35-44') AS "35-44",
    MAX(risk_index) FILTER (WHERE segment_value = '45-54') AS "45-54",
    MAX(risk_index) FILTER (WHERE segment_value = '55-64') AS "55-64",
    MAX(risk_index) FILTER (WHERE segment_value = '65+') AS "65+"
FROM risk_statistics
WHERE segment_type = 'AGE_BUCKET'
GROUP BY calculation_month
ORDER BY time;