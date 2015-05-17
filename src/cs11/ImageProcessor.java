package cs11;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by sydney on 17.05.15.
 */
public class ImageProcessor {
    PImage img;
    HScrollbar thresholdBar;
    PApplet parent;

    public void draw() {
        // f = 700
        parent.background(parent.color(0, 0, 0));

        PImage selecHueImg = selectHue(img, 115, 135); // 100 - 135
        PImage thresholdImg = thresholdImg(parent, selecHueImg, 0.05);
        //		print(thresholdBar.getPos());
        //		PImage blurImg = gaussianBlur(thresholdImg);
        PImage sobelImg = sobel(thresholdImg, 0.5);
        parent.image(img, 0, 0);
        hough(sobelImg);
        //		thresholdBar.display();
        //		thresholdBar.update();
    }
    private PImage thresholdImg(PApplet parent, PImage img, double threashold){
        PImage result = parent.createImage(parent.width, parent.height, parent.RGB); // create a new, initially transparent, 'result' image
        double threshold = threashold * 255;
        for(int i = 0; i < img.width * img.height; i++) {
            result.pixels[i] = parent.brightness(img.pixels[i])  > threshold? parent.color(255, 255, 255): parent.color(0, 0, 0);
        }
        return result;
    }

    public PImage sobel(PImage img, double max) {
        float[][] hKernel = {
                { 0, 1, 0 },
                { 0, 0, 0 },
                { 0, -1, 0}};
        float[][] vKernel = {
                { 0, 0, 0 },
                { 1, 0, -1},
                { 0, 0, 0 }};

        PImage result = parent.createImage(img.width, img.height, parent.ALPHA);

        // clear the image
        for (int i = 0; i < img.width * img.height; i++) {
            result.pixels[i] = parent.color(0);
        }

        for (int y=2; y < result.height-2; y++) {
            for (int x=2; x < result.width-2; x++) {
                int convH = 0;
                int convV = 0;

                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        convH += parent.brightness(img.get(x + i - 1, y + j - 1)) * hKernel[i][j];
                        convV += parent.brightness(img.get(x + i - 1, y + j - 1)) * vKernel[i][j];
                    }
                }
                if (Math.sqrt(convH * convH + convV * convV) > (int)(max* 255 * 0.3f)) { // 30% of the max
                    result.set(x, y, parent.color(255));
                } else {
                    result.set(x, y, parent.color(0));
                }


            }
        }
        return result;
    }

    public PImage convolute(PImage img) {
        float[][] kernel = {
                { 0, 0, 0 },
                { 0, 2, 0 },
                { 0, 0, 0 }};

        return transformAlgo(img, kernel, 1.f);
    }

    public PImage gaussianBlur(PImage img) {
        float[][] kernel = {
                { 9, 12, 9 },
                { 12, 15, 12 },
                { 9, 12, 9 }};


        return transformAlgo(img, kernel, 99.f);
    }

    private PImage selectHue(PImage img, float infHue, float supHue){
        // create a greyscale image (type: ALPHA) for output
        PImage result = parent.createImage(img.width, img.height, parent.ALPHA);

        for (int y =0; y<img.height; y++) {
            for (int x=0; x<img.width; x++) {
                float hue = parent.hue(img.get(x, y));
                if(infHue<hue && hue<supHue){
                    result.set(x, y, img.get(x, y));
                }
                else {
                    result.set(x, y, parent.color(0));
                }
            }
        }
        return result;
    }

    private PImage transformAlgo(PImage img, float[][] kernel, float weight) {

        // create a greyscale image (type: ALPHA) for output
        PImage result = parent.createImage(img.width, img.height, parent.ALPHA);

        for (int y =0; y<img.height; y++) {
            for (int x=0; x<img.width; x++) {

                float conv = 0;
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if(!(x+i-1 < 0 || x+i-1 > img.width-1 || y+j-1 < 0 || y+j-1 > img.height-1)){
                            conv += parent.brightness(img.get(x + i - 1, y + j - 1)) * kernel[i][j];
                        }
                    }
                }
                result.set(x, y, parent.color((int) (conv / weight)));
            }
        }
        return result;
    }

    public void hough(PImage edgeImg) {

        float discretizationStepsPhi = 0.01f;
        float discretizationStepsR = 2.5f;


        // dimensions of the accumulator
        int phiDim = (int) (Math.PI / discretizationStepsPhi);
        int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);
        // our accumulator (with a 1 pix margin around)
        final int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];


        // Fill the accumulator: on edge points (ie, white pixels of the edge
        // image), store all possible (r, phi) pairs describing lines going
        // through the point.
        for (int y = 0; y < edgeImg.height; y++) {
            for (int x = 0; x < edgeImg.width; x++) {
                // Are we on an edge?
                if (parent.brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
                    for(int phiIdx=0; phiIdx<phiDim; ++phiIdx){
                        double phi = discretizationStepsPhi * phiIdx;
                        double r = x*Math.cos(phi) + y*Math.sin(phi);

                        int rIdx = (int) ( (r/discretizationStepsR) + 0.5*(rDim-1) + 1) ;
                        accumulator[(phiIdx+1)*(rDim + 2) + rIdx] += 1;
                    }
                }
            }
        }




        ArrayList<Integer> bestCandidates = new ArrayList<Integer>();


        // size of the region we search for a local maximum
        int neighbourhood = 10;

        // only search around lines with more that this amount of votes
        // (to be adapted to your image)
        int minVotes = 330;
        for (int accR = 0; accR < rDim; accR++) {
            for (int accPhi = 0; accPhi < phiDim; accPhi++) {

                // compute current index in the accumulator
                int idx = (accPhi + 1) * (rDim + 2) + accR + 1;
                parent.print(accumulator[idx]);
                if (accumulator[idx] > minVotes) {

                    boolean bestCandidate = true;

                    // iterate over the neighbourhood
                    for(int dPhi=-neighbourhood/2; dPhi < neighbourhood/2+1; dPhi++) {

                        // check we are not outside the image
                        if( !(accPhi+dPhi < 0 || accPhi+dPhi >= phiDim)){
                            for(int dR=-neighbourhood/2; dR < neighbourhood/2 +1; dR++) {

                                // check we are not outside the image
                                if(!(accR+dR < 0 || accR+dR >= rDim)){
                                    int neighbourIdx = (accPhi + dPhi + 1) * (rDim + 2) + accR + dR + 1;
                                    // the current idx is not a local maximum!
                                    if(accumulator[idx] < accumulator[neighbourIdx]) {
                                        bestCandidate=false;
                                        break;
                                    }
                                }

                            }
                            if(!bestCandidate) break;
                        }

                    }
                    if(bestCandidate) {
                        // the current idx *is* a local maximum
                        bestCandidates.add(idx);
                    }
                }
            }
        }

        Collections.sort(bestCandidates, new Comparator<Integer>() {
            @Override
            public int compare(Integer l1, Integer l2) {
                if (accumulator[l1] > accumulator[l2]
                        || (accumulator[l1] == accumulator[l2] && l1 < l2)) return -1;
                return 1;
            }
        });


        for (int i = 0; i < bestCandidates.size(); i++) {
            int idx = bestCandidates.get(i);
            int accPhi = (int) (idx / (rDim + 2)) - 1;
            int accR = idx - (accPhi + 1) * (rDim + 2) - 1;
            float r = (accR - (rDim - 1) * 0.5f) * discretizationStepsR;
            float phi = accPhi * discretizationStepsPhi;


            // Cartesian equation of a line: y = ax + b
            // in polar, y = (-cos(phi)/sin(phi))x + (r/sin(phi))
            // => y = 0 : x = r / cos(phi)
            // => x = 0 : y = r / sin(phi)
            // compute the intersection of this line with the 4 borders of // the image
            int x0 = 0;
            int y0 = (int) (r / Math.sin(phi));
            int x1 = (int) (r / Math.cos(phi));
            int y1 = 0;
            int x2 = edgeImg.width;
            int y2 = (int) (-Math.cos(phi) / Math.sin(phi) * x2 + r / Math.sin(phi)); int y3 = edgeImg.width;
            int x3 = (int) (-(y3 - r / Math.sin(phi)) * (Math.sin(phi) / Math.cos(phi)));
            // Finally, plot the lines
            parent.stroke(204, 102, 0); if (y0 > 0) {
                if (x1 > 0)
                    parent.line(x0, y0, x1, y1);
                else if (y2 > 0)
                    parent.line(x0, y0, x2, y2);
                else
                    parent.line(x0, y0, x3, y3);
            }
            else {
                if (x1 > 0) {
                    if (y2 > 0)
                        parent.line(x1, y1, x2, y2); else
                        parent.line(x1, y1, x3, y3);
                }
                else
                    parent.line(x2, y2, x3, y3);
            }

        }


    }
}
