//
// Inspired by https://github.com/dog-qiuqiu/Android_NCNN_yolov4-tiny
//

#ifndef YOLO_H
#define YOLO_H

#include "ncnn/net.h"

namespace cv{
    typedef struct{
        int width;
        int height;
    } Size;
}

typedef struct BoxInfo {
    float x1;
    float y1;
    float x2;
    float y2;
    float score;
    int label;
} BoxInfo;

class Yolo {
public:
    Yolo(AAssetManager* mgr, const char* param, const char* bin);
    ~Yolo();
    std::vector<BoxInfo> detect(JNIEnv* env, jobject image);
private:
    static std::vector<BoxInfo> decode_infer(ncnn::Mat &data,const cv::Size& frame_size);
    ncnn::Net* Net;
    int input_size = 416;
public:
    static Yolo *detector;
};


#endif //YOLO_H
