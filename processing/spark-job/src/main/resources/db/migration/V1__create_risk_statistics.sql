CREATE TABLE risk_statistics (
    id BIGSERIAL PRIMARY KEY,
    segment_type VARCHAR(50) NOT NULL,
    calculation_month VARCHAR(7) NOT NULL,
    segment_value VARCHAR(50) NOT NULL,
    sample_size BIGINT NOT NULL,
    avg_claim_cost NUMERIC(18,2),
    avg_total_claim_cost NUMERIC(18,2),
    avg_policy_premium NUMERIC(18,2),
    median_claim_cost NUMERIC(18,2),
    median_total_claim_cost NUMERIC(18,2),
    avg_severity_score NUMERIC(10,4),
    loss_ratio NUMERIC(10,4),
    fraud_rate NUMERIC(10,4),
    risk_index NUMERIC(10,4),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(segment_type, calculation_month, segment_value)
);