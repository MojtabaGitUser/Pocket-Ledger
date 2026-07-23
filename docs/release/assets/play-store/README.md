# Play Store Asset Package

This directory contains the reviewed, repository-ready graphics for the Folentra
Play Store listing.

## Files

- `icon-512.png`: opaque 512 x 512 export of the production launcher artwork.
- `feature-graphic.png`: opaque 1024 x 500 branded feature graphic.
- `phone/`: four deterministic Paparazzi captures at 461 x 1000.
- `tablet/`: three deterministic adaptive captures at 1000 x 625.
- `source/feature-background.png`: generated abstract source used by the feature graphic.

The screenshots are copied from committed Paparazzi golden images and contain
only deterministic sample data. Regenerate screenshot sources through the
normal screenshot test workflow before replacing them.

Validate the committed package from the repository root:

```bash
python scripts/validate_play_store_assets.py
```

The release-candidate workflow runs this validation before building Android
artifacts. Play Console upload and final policy review remain release-owner
steps.
