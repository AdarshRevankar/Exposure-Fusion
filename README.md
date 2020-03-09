# High Dynamic Range (HDR) - Android [![version](https://img.shields.io/badge/version-1.0.1-yellow.svg)](https://semver.org)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/dwyl/esta/issues)
### Exposure Fusion Algorithm

Library created for the demonstration of computation of HDR on Android Device. This library uses <b>RenderScript</b> extensively for the computation of pixels.

This Application is provided with beautiful interface, so as to provide the demonstration of the usage of the <b>HDR Library</b>.

###### Install
In Android Studio users can import the ```.aar``` module from the release build / can include the library ```hdr``` to their project.

1. Open ```Project Structure > All Dependencies > +``` select the ```.aar``` file
2. Import the module to the current project & Apply

###### Usage
Library provides a single way so that multiple request can be sent.

1. In your project import ```com.adrino.hdr.corehdr.CreateHDR```
2. Create instance of the CreateHDR,
   ```
        CreateHDR createHdr = new CreateHDR(getApplicationContext());
   ```
3. Using ```perform``` method, perform any action ( intermediate step ) of Exposure Fusion by passing ```List<Bitmap>``` consisting of Multiple Exposed Images ( Size should be 3 )
   ```
        List<Bitmap> hdrImage = createHdr.perform( bitmapImageList, CreateHDR.Actions.HDR );
   ```
   Here, ```CreateHDR.Actions``` is enumeration which has following items,
   ```
        HDR
        CONTRAST
        EXPOSED
        GAUSSIAN
        LAPLACIAN
        NORMAL
        RESULTANT
        SATURATION
   ```
   Which is used to specify which intermediate result is required. Accordingly the size of output list varies. <i>{see documentation in CreateHDR }</i>
4. Using this list image can be used for further processing.

##### Contact
If any error, suggestions please do contact :

``` Adarsh Revankar ``` adarsh_revankar@live.com
