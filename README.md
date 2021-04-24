APNG builder
==
Java API to create an Animated PNG file.

Created for the [Ganimed](https://github.com/Moon70/Ganimed) tool, therefore the current interface is very limited.

This project is 'work in progress':

- **Version 0.0.1:** 

  - Simply puts existing imagedata together and inserts the needed APNG chunks.
  
- **Version 0.2-SNAPSHOT:**
  
  - Added own PNG encoder
  - Added APNG filtering
  - Added Frame cutting

- **Version 0.3-SNAPSHOT:**
  
  - Using transparent pixel to reduce filesize
  - Convert 24bit truecolour to 255 colour palette image, if lossless possible
  - Added Logger (SLF4J)

