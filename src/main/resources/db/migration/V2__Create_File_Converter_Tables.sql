-- Create file_conversions table
CREATE TABLE file_conversions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    original_format VARCHAR(50) NOT NULL,
    target_format VARCHAR(50) NOT NULL,
    original_file_size BIGINT NOT NULL,
    converted_file_size BIGINT,
    original_file_path VARCHAR(500) NOT NULL,
    converted_file_path VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    processing_time_ms BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_file_conversions_user_id ON file_conversions(user_id);
CREATE INDEX idx_file_conversions_status ON file_conversions(status);
CREATE INDEX idx_file_conversions_created_at ON file_conversions(created_at);
CREATE INDEX idx_file_conversions_user_status ON file_conversions(user_id, status);

-- Add foreign key constraint to users table (if it exists)
-- ALTER TABLE file_conversions ADD CONSTRAINT fk_file_conversions_user_id 
--     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Add comments for documentation
COMMENT ON TABLE file_conversions IS 'Stores file conversion requests and their status';
COMMENT ON COLUMN file_conversions.id IS 'Unique identifier for the conversion';
COMMENT ON COLUMN file_conversions.user_id IS 'ID of the user who requested the conversion';
COMMENT ON COLUMN file_conversions.original_filename IS 'Original filename of the uploaded file';
COMMENT ON COLUMN file_conversions.original_format IS 'Original file format (e.g., pdf, docx)';
COMMENT ON COLUMN file_conversions.target_format IS 'Target format for conversion (e.g., txt, pdf)';
COMMENT ON COLUMN file_conversions.original_file_size IS 'Size of the original file in bytes';
COMMENT ON COLUMN file_conversions.converted_file_size IS 'Size of the converted file in bytes';
COMMENT ON COLUMN file_conversions.original_file_path IS 'Path to the stored original file';
COMMENT ON COLUMN file_conversions.converted_file_path IS 'Path to the stored converted file';
COMMENT ON COLUMN file_conversions.status IS 'Current status of the conversion (PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED)';
COMMENT ON COLUMN file_conversions.error_message IS 'Error message if conversion failed';
COMMENT ON COLUMN file_conversions.processing_time_ms IS 'Time taken to process the conversion in milliseconds';
COMMENT ON COLUMN file_conversions.created_at IS 'Timestamp when the conversion was created';
COMMENT ON COLUMN file_conversions.updated_at IS 'Timestamp when the conversion was last updated';
COMMENT ON COLUMN file_conversions.completed_at IS 'Timestamp when the conversion was completed';