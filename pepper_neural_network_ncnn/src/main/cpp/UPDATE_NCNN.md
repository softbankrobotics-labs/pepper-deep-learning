## Update NCNN version

To update ncnn library version, download a new version from [https://github.com/Tencent/ncnn/releases](https://github.com/Tencent/ncnn/releases).

Choose `ncnn-<date>-android.zip`, do not take the vulkan version as it is supported only from Android 24, and Pepper tablet use Android 23.
Extract the zip file in this folder, and modify the line `set(NCNN_DIR ncnn-20201218-android)` in [CMakeLists.txt](CMakeLists.txt) to use your new ncnn version.
