package com.example.obstacledetection;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.Image;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DepthImageProcessor {

    public Bitmap ImageToBitmap(Image depthImage,int numRows, int[] lowBoundArr, int[] highBoundArr) {
        // The depth image has a single plane, which stores depth for each
        // pixel as 16-bit unsigned integers.

        Image.Plane plane = depthImage.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer().order(ByteOrder.nativeOrder());

        Bitmap bitmap = Bitmap.createBitmap(depthImage.getWidth(), depthImage.getHeight(), Bitmap.Config.ARGB_8888);
        int byteIndex;
        int dist;
        int width_increment = depthImage.getWidth()/numRows;

        for(int height=0;height<depthImage.getHeight(); height++){
            for(int width=0; width<depthImage.getWidth();width++){
                byteIndex = width * plane.getPixelStride() + height * plane.getRowStride();
                dist = buffer.getShort(byteIndex);
                if(dist<0) dist = 65536-dist;//to deal with overflowing due to signed shorts
                if(dist>=0 && dist<lowBoundArr[width/width_increment]){
                    bitmap.setPixel(width,height, Color.argb(128, 255,0,0));
                }
                else if(dist>=lowBoundArr[width/width_increment] && dist<highBoundArr[width/width_increment]){
                    bitmap.setPixel(width,height,Color.argb(128, 0,255,0));
                }
                else{
                    bitmap.setPixel(width,height,Color.argb(128, 0,0,255));
                }
            }
        }
        //rotate 90 degrees because depthImage is in landscape mode
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public int getAverageSubImageDist(Image depthImage, int heightstart, int heightend, int widthstart, int widthend){
        //starts inclusive, ends not inclusive
        Image.Plane plane = depthImage.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer().order(ByteOrder.nativeOrder());
        int byteIndex, dist, mean_value=0;
        for(int height=heightstart;height<heightend; height++){
            for(int width=widthstart; width<widthend;width++){
                byteIndex = width * plane.getPixelStride() + height * plane.getRowStride();
                dist = buffer.getShort(byteIndex);
                if(dist<0) dist = 65536-dist;//to deal with overflowing due to signed shorts
                mean_value += dist;
            }
        }
        return mean_value/((heightend-heightstart)*(widthend-widthstart));
    }

    public int[][] getAverageDistances(Image depthImage, int rows, int cols, int width_percentage){
        int [][] distance_matrix = new int[rows][cols];

        int true_height = (int) (depthImage.getHeight()*(width_percentage/100f));//rotated image
        int height_offset = (depthImage.getHeight() - true_height)/2;

        int height_increment = true_height/cols;//Image needs to be rotated 90 degrees so use cols instead of rows here
        int width_increment =  depthImage.getWidth()/rows;

        for(int i=0; i<rows;i++){//assume the image is horizontal
            for(int j=0;j<cols;j++){
                distance_matrix[i][cols-j-1] = getAverageSubImageDist(depthImage,height_offset + j*height_increment,height_offset + (j+1)*height_increment,i*width_increment,(i+1)*width_increment);
            }
        }

        return distance_matrix;
    }
}
