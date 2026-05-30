$ErrorActionPreference = "Stop"

$Image = "vaibhav990/booking-service"
$Tag = if ($args[0]) { $args[0] } else { "latest" }
$FullImage = "${Image}:${Tag}"

Write-Host "Building $FullImage ..." -ForegroundColor Cyan
docker build -t $FullImage .

Write-Host "Pushing $FullImage to Docker Hub ..." -ForegroundColor Cyan
docker push $FullImage

Write-Host "Done. Use this image on Render:" -ForegroundColor Green
Write-Host "  docker.io/$FullImage"
