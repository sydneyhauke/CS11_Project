import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;
import processing.video.Movie;

public class ImageProcessing extends PApplet {
	PImage img;
	HScrollbar infHue;
	HScrollbar supHue;
	HScrollbar infBrightness;
	HScrollbar supBrightness;
	HScrollbar infSaturation;
	HScrollbar supSaturation;

	Capture cam;

	public void setup() {
		size(1280, 785);

		String[] cameras = Capture.list();
		if (cameras.length == 0) {
			println("There are no cameras available for capture.");
			exit();
		} else {
			println("Available cameras:");
			for (int i = 0; i < cameras.length; i++) {
				println(i + ": " + cameras[i]);
			}
			cam = new Capture(this, cameras[15]);
			cam.start();
		}

		infHue = new HScrollbar(this, 100, 720, 1180, 10);
		supHue = new HScrollbar(this, 100, 731, 1180, 10);

		infBrightness = new HScrollbar(this, 100, 742, 1180, 10);
		supBrightness = new HScrollbar(this, 100, 753, 1180, 10);

		infSaturation = new HScrollbar(this, 100, 764, 1180, 10);
		supSaturation = new HScrollbar(this, 100, 775, 1180, 10);
	}

	public void draw() {
		background(255);

		if(cam.available()) {
			cam.read();
		}
		img = cam.get();

		infHue.update();
		supHue.update();

		infBrightness.update();
		supBrightness.update();

		infSaturation.update();
		supSaturation.update();

		PImage selecHueImg = selHSB(img, infHue.getPos() * 255, supHue.getPos() * 255, infSaturation.getPos() * 255, supSaturation.getPos() * 255, infBrightness.getPos() * 255, supBrightness.getPos() * 255); // 100 - 135 et webcam : 98, 112
		PImage bluredImg = gaussianBlur(gaussianBlur(selecHueImg));
		PImage thresholdImg = thresholdImg(bluredImg, 0.97);
		PImage sobelImg = sobel(thresholdImg, 0.1);

		image(img, 0, 0);
		
		QuadGraph qg = new QuadGraph();
		List<PVector> lines = hough(sobelImg, 200, 4);
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
			// Choose a random, semi-transparent colour
			Random random = new Random();
			fill(color(min(255, random.nextInt(300)),
					min(255, random.nextInt(300)),
					min(255, random.nextInt(300)), 50));
			quad(c12.x,c12.y,c23.x,c23.y,c34.x,c34.y,c41.x,c41.y);
		}

		infHue.display();
		supHue.display();

		infBrightness.display();
		supBrightness.display();

		infSaturation.display();
		supSaturation.display();

		textSize(8);

		text("infHue : " + infHue.getPos()*255, 10, 729);
		text("supHue : " + supHue.getPos()*255, 10, 740);

		text("infBright : " + infBrightness.getPos()*255, 10, 751);
		text("supBright : " + supBrightness.getPos()*255, 10, 762);

		text("infSat : " + infSaturation.getPos()*255, 10, 773);
		text("supSat : " + supSaturation.getPos()*255, 10, 784);
	}

	PImage selHSB(PImage src, float infHue, float supHue, float infSat, float supSat, float infBr, float supBr) {
		PImage result = createImage(src.width, src.height, ALPHA);

		int size = src.width * src.height;
		for(int i = 0; i < size; i++) {
			int pix = src.pixels[i];
			float br = brightness(pix);
			float hue = hue(pix);
			float sat = saturation(pix);

			if(infHue <= hue && hue <= supHue && infSat <= sat && sat <= supSat && infBr <= br && br <= supBr) {
				result.pixels[i] = color(255);
			}
			else {
				result.pixels[i] = color(0);
			}
		}

		return result;
	}

	PImage thresholdImg(PImage img, double threashold){
		PImage result = createImage(img.width, img.height, ALPHA); // create a new, initially transparent, 'result' image

		int size = img.width * img.height;
		double threshold = threashold * 255;
		for(int i = 0;	 i < size; i++) {
			result.pixels[i] = brightness(img.pixels[i])  > threshold ? color(255): color(0);
		}

		return result;
	}

	PImage thresholdInverseImg(PImage img, double threashold){
		PImage result = createImage(img.width, img.height, RGB); // create a new, initially transparent, 'result' image

		double threshold = threashold * 255;
		for(int i = 0;	 i < img.width * img.height; i++) {
			result.pixels[i] = brightness(img.pixels[i])  > threshold? color(0, 0, 0) : color(255, 255, 255); 
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
				{1, 0, -1},
				{0, 0, 0 }};

		PImage result = createImage(img.width, img.height, ALPHA);

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
						convH += brightness(img.pixels[(y+j-1) * img.width + x+i-1]) * hKernel[i][j];
						convV += brightness(img.pixels[(y+j-1) * img.width + x+i-1]) * vKernel[i][j];
					}
				}
				if (sqrt(convH*convH + convV*convV) > (int)(max* 255 * 0.3f)) { // 30% of the max
					result.pixels[y * img.width + x] = color(255);
				} else {
					result.pixels[y * img.width + x] = color(0);
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

	public PImage gaussianBlur(PImage src) {
		float[][] kernel = {
				{ 9, 12, 9 },
				{ 12, 15, 12 },
				{ 9, 12, 9 }};


		return transformAlgo(src, kernel, 99);
	}

	PImage selectHue(PImage src, float infHue, float supHue){
		// create a greyscale image (type: ALPHA) for output
		PImage result = createImage(src.width, src.height, ALPHA);

		for (int y =0; y<src.height; y++) {
			for (int x=0; x<src.width; x++) {
				float hue = hue(src.get(x, y));
				if(infHue<=hue && hue<=supHue){
					result.pixels[y*src.width + x] = color(255);
				}
				else {
					result.pixels[y*src.width + x] = color(0);
				}
			}
		}
		return result;
	}

	private PImage transformAlgo(PImage src, float[][] kernel, float weight) {
		// create a greyscale image (type: ALPHA) for output
		PImage result = createImage(src.width, src.height, ALPHA);

		for (int y =0; y<src.height; y++) {
			for (int x=0; x<src.width; x++) {

				float conv = 0;
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						if(!(x+i-1 < 0 || x+i-1 > src.width-1 || y+j-1 < 0 || y+j-1 > src.height-1)){
							conv += brightness(src.pixels[(y+j-1) * src.width + x+i-1]) * kernel[i][j];
						}
					}
				}
				result.pixels[y * src.width + x] = color(conv/weight);
			}
		}

		return result;
	}

	public ArrayList<PVector> hough(PImage edgeImg,  int minVotes, int nLines) {

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
				if (brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
					for(int phiIdx=0; phiIdx<phiDim; ++phiIdx){
						double phi = discretizationStepsPhi * phiIdx;
						double r = x*Math.cos(phi) + y*Math.sin(phi);

						int rIdx = (int) ( (r/discretizationStepsR) + 0.5*(rDim-1) + 1) ;
						accumulator[(phiIdx+1)*(rDim + 2) + rIdx] += 1;
					}
				}
			}
		}

		//display accumulator
		PImage houghImg = createImage(rDim + 2, phiDim + 2, ALPHA);
		for (int i = 0; i < accumulator.length; i++) {
		houghImg.pixels[i] = color(min(255, accumulator[i]));
		}
		houghImg.updatePixels();
//		System.out.println(houghImg.width + " " + houghImg.height);
		/*translate(800,0);
		image(houghImg,0,0);
		translate(-800, 0);*/
		
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
			int y0 = (int) (r / sin(phi));
			int x1 = (int) (r / cos(phi));
			int y1 = 0;
			int x2 = edgeImg.width;
			int y2 = (int) (-cos(phi) / sin(phi) * x2 + r / sin(phi)); int y3 = edgeImg.width;
			int x3 = (int) (-(y3 - r / sin(phi)) * (sin(phi) / cos(phi)));
			// Finally, plot the lines
			stroke(204,102,0);
			if (y0 > 0) {
				if (x1 > 0)
					line(x0, y0, x1, y1);
				else if (y2 > 0)
					line(x0, y0, x2, y2);
				else
					line(x0, y0, x3, y3);
			}
			else {
				if (x1 > 0) {
					if (y2 > 0)
						line(x1, y1, x2, y2); else
						line(x1, y1, x3, y3);
				}
				else
					line(x2, y2, x3, y3);
			}

		}
		return getIntersections(lines);

	}



	public ArrayList<PVector> getIntersections(List<PVector> lines) { 
		ArrayList<PVector> intersections = new ArrayList<PVector>(); 
		for (int i = 0; i < lines.size() - 1; i++) {
			PVector line1 = lines.get(i);
			for (int j = i + 1; j < lines.size(); j++) {
				PVector line2 = lines.get(j);
				
				PVector inter = getIntersection(line1, line2);

				fill(255, 128, 0);
				ellipse(inter.x, inter.y, 10, 10);
				
				intersections.add(inter);
			}
		}
		return intersections; 
	}

	public PVector getIntersection(PVector line1, PVector line2) { 

		// compute the intersection and add it to 'intersections' 
		// draw the intersection
		float d = cos(line2.y)*sin(line1.y) - cos(line1.y)*sin(line2.y);
		float x = (line2.x*sin(line1.y) - line1.x*sin(line2.y)) / d;
		float y = (-line2.x*cos(line1.y) + line1.x*cos(line2.y)) / d;

		ellipse(x, y, 10, 10);
		fill(255, 128, 0);
		return new PVector(x, y);

	}


}
