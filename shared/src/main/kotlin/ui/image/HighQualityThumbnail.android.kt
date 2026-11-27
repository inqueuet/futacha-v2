package com.valoser.futacha.shared.ui.image

import coil3.decode.BitmapFactoryDecoder
import coil3.request.ImageRequest

// The registered animated decoders (ImageDecoder, APNG, GIF) would play the
// original; BitmapFactory decodes the first frame only.
/**
 * Forces a single still frame so an animated original never starts playing
 * where the thumbnail was still. Platforms whose decoder is already static
 * leave the request unchanged.
 */
internal fun ImageRequest.Builder.staticImageDecoding(): ImageRequest.Builder =
    decoderFactory(BitmapFactoryDecoder.Factory())

// BitmapFactory and ImageDecoder already subsample to the requested size while decoding.
/**
 * Decodes the original straight to the slot size. Platforms whose decoder
 * already subsamples while decoding (Android) leave the request unchanged; the
 * Skia decoder on iOS decodes the full-resolution image before scaling it.
 */
internal fun ImageRequest.Builder.boundedOriginalDecoding(): ImageRequest.Builder = this
