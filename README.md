APNG builder
==
Java API to create an Animated PNG file.

Created for the [Ganimed](https://github.com/Moon70/Ganimed) tool.

Features:

- Creates an APNG from a series of files or BufferedImage objects.
- Images get cropped, if top/bottom/left/right area is unchanged from the previous image.
- Optional: Replace pixel, that are unchanged from the previous image, with transparent pixel. Depending on the animation, this can drastically reduce filesize.
- Optional: Reducing bit depth. Depending on the animation, reducing 24bit truecolour to 21bit or 18bit is often hard to recognizable, but reduces filesize. This option is not lossless.
- Optional: Converting 24bit truecolour to 255 (256) colour palette, using an own colour quantizer (see [GPAC](https://github.com/Moon70/GPAC)).

**<u>This project is 'work in progress'.</u>**

<u>Please notes: As long as version 1.0 is not final, i feel free to change the inferface any time :-)</u>

For more details see the [wiki](https://github.com/Moon70/APNG-builder/wiki) pages, please.