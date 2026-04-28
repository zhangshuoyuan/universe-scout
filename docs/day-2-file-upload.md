# Day 2 File Upload

Day 2 completes the local file intake path.

## Completed

- Image upload endpoint: `POST /api/files/upload/images`
- Zip upload endpoint: `POST /api/files/upload/zip`
- Upload batch page endpoint: `GET /api/files/batches`
- Upload batch detail endpoint: `GET /api/files/batches/{batchId}`
- Local upload directory creation
- Local extract directory creation
- `upload_batch` records
- `upload_file` records
- Frontend upload center
- Frontend batch list
- Frontend batch detail

## Status Values

`upload_batch.source_type`:

- `IMAGE_BATCH`
- `ZIP`

`upload_batch.status`:

- `UPLOADED`
- `FAILED`

`upload_file.status`:

- `UPLOADED`
- `FAILED`

## Local Paths

- Uploaded files: `D:/universe-scout/uploads`
- Extracted images: `D:/universe-scout/extracted`

## Notes

Day 2 does not call the vision model, create parse tasks, or parse image content.
