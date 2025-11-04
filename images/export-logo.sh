#!/usr/bin/env sh

export_png() {
  file="logo-$1.png"
  file_min="logo-$1.min.png"
  inkscape --export-area-drawing --export-width="$1" --export-height="$1" --export-filename="$file" logo.svg
  pngquant --quality 20-80 "$file" -o "$file_min" -f --skip-if-larger
  mv "$file_min" "$file"
}

export_png 48
export_png 128
