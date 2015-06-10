import processing.core.PApplet;
import processing.core.PImage;
import processing.video.*;

public class HoughTransform extends PApplet {
	Capture cam;
	PImage img;
	HScrollbar hue;
	HScrollbar brightness;
	HScrollbar saturation;

	public void setup() {
		size(800, 600);
		/*String[] cameras = Capture.list();
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
		}*/
		img = loadImage("board1.jpg");

		hue = new HScrollbar(this, 0, 520, 800, 20);
		saturation = new HScrollbar(this, 0, 550, 800, 20);
		brightness = new HScrollbar(this, 0, 580, 800, 20);
	}

	public void draw() {
		/*if (cam.available()) {
			cam.read();
		}

		img = cam.get();
		image(convolute(img), 0, 0);*/

		hue.update();
		brightness.update();
		saturation.update();

		image(selHSB(img, hue.getPos(), saturation.getPos(), brightness.getPos()), 0, 0);

		hue.display();
		brightness.display();
		saturation.display();

	}

	private PImage selHSB(PImage src, float hue, float saturation, float brightness) {
		PImage result = createImage(src.width, src.height, RGB);

		for(int i = 0; i < src.width * src.height; i++) {
			int pix = src.pixels[i];
			if(hue(pix) > hue && brightness(pix) > brightness && saturation(pix) > saturation) {
				result.pixels[i] = pix;
			}
			else {
				result.pixels[i] = color(0);
			}
		}

		return result;
	}

	private PImage convolute(PImage src) {
		float[][] kernel = {
			{9,12,9},
			{12,15,12},
			{9,12,9}
		};

		float weight = 99;
		PImage result = createImage(src.width, src.height, ALPHA);
		int w = src.width;
		int h = src.height;

		for(int x = 1; x < w-1; x++) {
			for(int y = 1; y < h-1; y++) {
				float accum = 0;

				for(int j = 0; j < 3; j++) {
					for(int k = 0; k < 3; k++) {
						//if(!((x-1) < 0 || (x+1) > w-1 || (y-1) < 0 || (y+1) > h-1)) {
							accum += brightness(src.get(x, y)) * kernel[j][k];
						//}
					}
				}

				result.set(x, y, color((int)min((accum/weight), 255)));
			}
		}

		return result;
	}
}