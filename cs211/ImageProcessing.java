import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;


public class ImageProcessing {
    PApplet parent;

    public ImageProcessing(PApplet parent) {
        this.parent = parent;
    }

    /**
     * Computes the position of all four corners of the board with the help of an image
     *
     * @param img
     * @param infHue
     * @param supHue
     * @param infSat
     * @param supSat
     * @param infBr
     * @param supBr
     * @return List of all positions of the corners
     */
	public List<PVector> process(PImage img, float infHue, float supHue, float infSat, float supSat, float infBr, float supBr) {
		PImage selecHueImg = selHSB(img, infHue, supHue, infSat, supSat, infBr, supBr); // 100 - 135 et webcam : 98, 112
		PImage bluredImg = gaussianBlur(gaussianBlur(selecHueImg));
		PImage thresholdImg = thresholdImg(bluredImg, 0.97);
		PImage sobelImg = sobel(thresholdImg, 0.1);
		
		QuadGraph qg = new QuadGraph();
		List<PVector> lines = hough(sobelImg, 200, 6);
		qg.build(lines, img.width, img.height);
		qg.findCycles(400, 100);

		for (int[] quad : qg.cycles) {
			PVector l1 = lines.get(quad[0]);
			PVector l2 = lines.get(quad[1]);
			PVector l3 = lines.get(quad[2]);
			PVector l4 = lines.get(quad[3]);

			// (intersection() is a simplified version of the
			// intersections() method you wrote last week, that simply
			// return the coordinates of the intersection between 2 lines)
			PVector c12 = getIntersection(l1, l2);
			PVector c23 = getIntersection(l2, l3);
			PVector c34 = getIntersection(l3, l4);
			PVector c41 = getIntersection(l4, l1);

            if(QuadGraph.nonFlatQuad(c12, c23, c34, c41) && QuadGraph.isConvex(c12, c23, c34, c41)) {
                List<PVector> lst = new ArrayList<PVector>();
                lst.add(c12);
                lst.add(c23);
                lst.add(c34);
                lst.add(c41);

                return lst;
            }
		}

        return new ArrayList<>(); // return empty list if no good quad is found
	}

	private PImage selHSB(PImage src, float infHue, float supHue, float infSat, float supSat, float infBr, float supBr) {
		PImage result = parent.createImage(src.width, src.height, PApplet.ALPHA);

		int size = src.width * src.height;
		for(int i = 0; i < size; i++) {
			int pix = src.pixels[i];
			float br = parent.brightness(pix);
			float hue = parent.hue(pix);
			float sat = parent.saturation(pix);

			if(infHue <= hue && hue <= supHue && infSat <= sat && sat <= supSat && infBr <= br && br <= supBr) {
				result.pixels[i] = parent.color(255);
			}
			else {
				result.pixels[i] = parent.color(0);
			}
		}

		return result;
	}

	private PImage thresholdImg(PImage img, double threashold){
		PImage result = parent.createImage(img.width, img.height, PApplet.ALPHA); // create a new, initially transparent, 'result' image

		int size = img.width * img.height;
		double threshold = threashold * 255;
		for(int i = 0;	 i < size; i++) {
			result.pixels[i] = parent.brightness(img.pixels[i])  > threshold ? parent.color(255): parent.color(0);
		}

		return result;
	}

	private PImage thresholdInverseImg(PImage img, double threashold){
		PImage result = parent.createImage(img.width, img.height, PApplet.RGB); // create a new, initially transparent, 'result' image

		double threshold = threashold * 255;
		for(int i = 0;	 i < img.width * img.height; i++) {
			result.pixels[i] = parent.brightness(img.pixels[i])  > threshold ? parent.color(0, 0, 0) : parent.color(255, 255, 255);
		}

		return result;
	}

	private PImage sobel(PImage img, double max) {
		float[][] hKernel = {
				{ 0, 1, 0 },
				{ 0, 0, 0 },
				{ 0, -1, 0}};
		float[][] vKernel = {
				{ 0, 0, 0 },
				{1, 0, -1},
				{0, 0, 0 }};

		PImage result = parent.createImage(img.width, img.height, PApplet.ALPHA);

		// clear the image
		/*for (int i = 0; i < img.width * img.height; i++) {
			result.pixels[i] = color(0);
		}*/

		for (int y=2; y < result.height-2; y++) {
			for (int x=2; x < result.width-2; x++) {
				int convH = 0;
				int convV = 0;

				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						convH += parent.brightness(img.pixels[(y + j - 1) * img.width + x + i - 1]) * hKernel[i][j];
						convV += parent.brightness(img.pixels[(y + j - 1) * img.width + x + i - 1]) * vKernel[i][j];
					}
				}
				if (PApplet.sqrt(convH * convH + convV * convV) > (int)(max* 255 * 0.3f)) { // 30% of the max
					result.pixels[y * img.width + x] = parent.color(255);
				} else {
					result.pixels[y * img.width + x] = parent.color(0);
				}


			}
		}
		return result;
	}

	private PImage convolute(PImage img) {
		float[][] kernel = {
				{ 0, 0, 0 },
				{ 0, 2, 0 },
				{ 0, 0, 0 }};

		return transformAlgo(img, kernel, 1.f);
	}

	private PImage gaussianBlur(PImage src) {
		float[][] kernel = {
				{ 9, 12, 9 },
				{ 12, 15, 12 },
				{ 9, 12, 9 }};


		return transformAlgo(src, kernel, 99);
	}

	private PImage transformAlgo(PImage src, float[][] kernel, float weight) {
		// create a greyscale image (type: ALPHA) for output
		PImage result = parent.createImage(src.width, src.height, PApplet.ALPHA);

		for (int y =0; y<src.height; y++) {
			for (int x=0; x<src.width; x++) {

				float conv = 0;
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						if(!(x+i-1 < 0 || x+i-1 > src.width-1 || y+j-1 < 0 || y+j-1 > src.height-1)){
							conv += parent.brightness(src.pixels[(y + j - 1) * src.width + x + i - 1]) * kernel[i][j];
						}
					}
				}
				result.pixels[y * src.width + x] = parent.color(conv / weight);
			}
		}

		return result;
	}

	private List<PVector> hough(PImage edgeImg,  int minVotes, int nLines) {

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
                    for (int phiIdx = 0; phiIdx < phiDim; ++phiIdx) {
                        double phi = discretizationStepsPhi * phiIdx;
                        double r = x * Math.cos(phi) + y * Math.sin(phi);

                        int rIdx = (int) ((r / discretizationStepsR) + 0.5 * (rDim - 1) + 1);
                        accumulator[(phiIdx + 1) * (rDim + 2) + rIdx] += 1;
                    }
                }
            }
        }
		
		ArrayList<Integer> bestCandidates = new ArrayList<Integer>();

		// size of the region we search for a local maximum
		int neighbourhood = 10;

		// only search around lines with more that this amount of votes
		// (to be adapted to your image)
		for (int accR = 0; accR < rDim; accR++) {
			for (int accPhi = 0; accPhi < phiDim; accPhi++) {

				// compute current index in the accumulator
				int idx = (accPhi + 1) * (rDim + 2) + accR + 1;
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

		Collections.sort(bestCandidates, new Comparator<Integer>(){
			@Override
			public int compare(Integer l1, Integer l2) {
				if (accumulator[l1] > accumulator[l2]
						|| (accumulator[l1] == accumulator[l2] && l1 < l2)) return -1;
				return 1;
			}
		});


		List<PVector> lines = new ArrayList<>();

		for (int i = 0; i < Math.min(bestCandidates.size(), nLines); i++) {
			int idx = bestCandidates.get(i);
			int accPhi = (int) (idx / (rDim + 2)) - 1;
			int accR = idx - (accPhi + 1) * (rDim + 2) - 1;
			float r = (accR - (rDim - 1) * 0.5f) * discretizationStepsR;
			float phi = accPhi * discretizationStepsPhi;

			lines.add(new PVector(r, phi));


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
			int y2 = (int) (-Math.cos(phi) / Math.sin(phi) * x2 + r / Math.sin(phi));
            int y3 = edgeImg.width;
			int x3 = (int) (-(y3 - r / Math.sin(phi)) * (Math.sin(phi) / Math.cos(phi)));
			// Finally, plot the lines
			parent.stroke(204, 102, 0);
			if (y0 > 0) {
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
						parent.line(x1, y1, x2, y2);
                    else
						parent.line(x1, y1, x3, y3);
				}
				else
					parent.line(x2, y2, x3, y3);
			}

		}
		return getIntersections(lines);

	}

	private List<PVector> getIntersections(List<PVector> lines) {
		ArrayList<PVector> intersections = new ArrayList<PVector>(); 
		for (int i = 0; i < lines.size() - 1; i++) {
			PVector line1 = lines.get(i);
			for (int j = i + 1; j < lines.size(); j++) {
				PVector line2 = lines.get(j);
				
				PVector inter = getIntersection(line1, line2);

				parent.fill(255, 128, 0);
				parent.ellipse(inter.x, inter.y, 10, 10);
				
				intersections.add(inter);
			}
		}
		return intersections; 
	}

	private PVector getIntersection(PVector line1, PVector line2) {

		// compute the intersection and add it to 'intersections' 
		// draw the intersection
		float d = PApplet.cos(line2.y)*PApplet.sin(line1.y) - PApplet.cos(line1.y)*PApplet.sin(line2.y);
		float x = (line2.x*PApplet.sin(line1.y) - line1.x*PApplet.sin(line2.y)) / d;
		float y = (-line2.x*PApplet.cos(line1.y) + line1.x*PApplet.cos(line2.y)) / d;

		parent.ellipse(x, y, 10, 10);
		parent.fill(255, 128, 0);

		return new PVector(x, y);
	}
}
