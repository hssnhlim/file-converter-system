# File Converter Feature

## Overview

The File Converter feature is a production-grade, scalable solution for converting files between different formats. It's designed with industry best practices and can be easily extended for future enhancements like Kafka integration, file scanning, and S3 storage.

## Architecture

### Components

1. **Entity (`FileConversion`)**: JPA entity for tracking conversion requests and their status
2. **Repository (`FileConversionRepository`)**: Data access layer with custom queries
3. **DTOs**: Request/Response objects for API communication
4. **Service (`FileConverterService`)**: Business logic and file conversion implementation
5. **Controller (`FileConverterController`)**: REST API endpoints
6. **Exceptions**: Custom exception classes for different error scenarios
7. **Configuration**: Async processing and file converter settings

### Design Patterns

- **Layered Architecture**: Clear separation of concerns
- **Repository Pattern**: Abstracted data access
- **DTO Pattern**: Clean API contracts
- **Builder Pattern**: Immutable object construction
- **Async Processing**: Non-blocking file conversion
- **Exception Handling**: Comprehensive error management

## Features

### Current Implementation

- **File Upload & Conversion**: Support for multiple file formats
- **Async Processing**: Non-blocking conversion with status tracking
- **Format Validation**: Checks for supported conversion pairs
- **File Size Validation**: Configurable maximum file size limits
- **Status Tracking**: Real-time conversion status updates
- **Download Support**: Secure file download after conversion
- **User Isolation**: Each user can only access their own conversions
- **Pagination**: Efficient handling of large conversion lists

### Supported Formats

#### Document Formats
- **PDF** ↔ **TXT**, **DOCX**
- **DOCX** ↔ **PDF**, **TXT**
- **TXT** ↔ **PDF**, **DOCX**

#### Image Formats
- **JPG** ↔ **PNG**, **GIF**, **BMP**
- **PNG** ↔ **JPG**, **GIF**, **BMP**
- **GIF** ↔ **JPG**, **PNG**, **BMP**
- **BMP** ↔ **JPG**, **PNG**, **GIF**

#### Video Formats
- **MP4** ↔ **AVI**, **MOV**
- **AVI** ↔ **MP4**, **MOV**
- **MOV** ↔ **MP4**, **AVI**

## API Endpoints

### File Conversion
- `POST /api/v1/file-converter/convert` - Convert a file to target format
- `GET /api/v1/file-converter/conversions` - Get user's conversions
- `GET /api/v1/file-converter/conversions/paginated` - Get paginated conversions
- `GET /api/v1/file-converter/conversions/{id}` - Get conversion status
- `GET /api/v1/file-converter/conversions/{id}/download` - Download converted file
- `DELETE /api/v1/file-converter/conversions/{id}` - Cancel conversion

### Format Information
- `GET /api/v1/file-converter/formats` - Get supported formats
- `GET /api/v1/file-converter/formats/check` - Check format compatibility

## Configuration

### Application Properties

```yaml
file:
  converter:
    max-file-size: 104857600  # 100MB
    storage:
      path: /tmp/converted-files
    supported-formats: pdf,docx,txt,jpg,png,gif,bmp,mp4,avi,mov
    async:
      core-pool-size: 2
      max-pool-size: 5
      queue-capacity: 100
    cleanup:
      retention-hours: 24
      enabled: true
```

### Environment Variables

- `FILE_CONVERTER_MAX_FILE_SIZE`: Maximum file size in bytes
- `FILE_CONVERTER_STORAGE_PATH`: Storage directory path
- `FILE_CONVERTER_ASYNC_CORE_POOL_SIZE`: Async thread pool core size
- `FILE_CONVERTER_ASYNC_MAX_POOL_SIZE`: Async thread pool max size

## Security Features

- **Authentication Required**: All endpoints require valid authentication
- **User Isolation**: Users can only access their own conversions
- **File Validation**: File size and format validation
- **Secure Downloads**: Direct file access prevention

## Performance Features

- **Async Processing**: Non-blocking file conversion
- **Thread Pool Management**: Configurable thread pools
- **File Streaming**: Efficient file handling
- **Database Indexing**: Optimized queries with custom indexes

## Error Handling

### Custom Exceptions

- `FileConversionException`: General conversion errors
- `UnsupportedFormatException`: Unsupported format combinations
- `FileTooLargeException`: File size exceeds limits

### HTTP Status Codes

- `200 OK`: Successful operation
- `202 Accepted`: Conversion accepted for processing
- `400 Bad Request`: Invalid request or unsupported format
- `413 Payload Too Large`: File exceeds size limit
- `404 Not Found`: Conversion not found
- `500 Internal Server Error`: Server-side errors

## Future Enhancements

### Phase 1: Robustness
- **Kafka Integration**: Message queuing for high-volume processing
- **File Scanning**: Antivirus and malware detection
- **S3 Integration**: Cloud storage for converted files
- **Redis Caching**: Performance optimization

### Phase 2: Advanced Features
- **Batch Processing**: Multiple file conversion
- **Format Detection**: Automatic format recognition
- **Quality Settings**: Configurable conversion quality
- **Progress Tracking**: Real-time conversion progress

### Phase 3: Enterprise Features
- **Multi-tenant Support**: Organization-level isolation
- **Audit Logging**: Comprehensive activity tracking
- **Rate Limiting**: Usage-based throttling
- **Webhook Support**: Event notifications

## Database Schema

### File Conversions Table

```sql
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

-- Indexes for performance
CREATE INDEX idx_file_conversions_user_id ON file_conversions(user_id);
CREATE INDEX idx_file_conversions_status ON file_conversions(status);
CREATE INDEX idx_file_conversions_created_at ON file_conversions(created_at);
```

## Testing

### Unit Tests
- Service layer business logic
- Repository data access
- Exception handling
- Format validation

### Integration Tests
- API endpoint functionality
- Database operations
- File processing workflows
- Security validations

### Performance Tests
- Concurrent conversion handling
- Large file processing
- Database query performance
- Memory usage optimization

## Monitoring & Observability

### Metrics
- Conversion success/failure rates
- Processing time distribution
- File size statistics
- User activity patterns

### Logging
- Structured logging with correlation IDs
- Performance metrics logging
- Error tracking and alerting
- Audit trail maintenance

### Health Checks
- Service availability monitoring
- Database connectivity checks
- Storage capacity monitoring
- Async queue health status

## Deployment Considerations

### Infrastructure
- **Horizontal Scaling**: Multiple service instances
- **Load Balancing**: Traffic distribution
- **Database Clustering**: High availability
- **Storage Redundancy**: File backup strategies

### Security
- **HTTPS Enforcement**: Secure communication
- **API Rate Limiting**: Abuse prevention
- **File Type Validation**: Security scanning
- **Access Control**: Role-based permissions

### Performance
- **CDN Integration**: Global file distribution
- **Database Optimization**: Query performance tuning
- **Caching Strategy**: Response time improvement
- **Resource Monitoring**: Capacity planning

## Contributing

### Development Setup
1. Clone the repository
2. Install dependencies: `./gradlew build`
3. Configure database connection
4. Set environment variables
5. Run the application: `./gradlew bootRun`

### Code Standards
- Follow Java coding conventions
- Use meaningful variable names
- Add comprehensive documentation
- Include unit tests for new features
- Follow SOLID principles

### Testing Guidelines
- Maintain >80% code coverage
- Test both success and failure scenarios
- Mock external dependencies
- Use test containers for integration tests

## License

This feature is part of the Crumoria System and follows the same licensing terms.