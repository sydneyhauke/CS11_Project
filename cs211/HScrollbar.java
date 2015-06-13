import processing.core.PApplet;

public class HScrollbar {
	PApplet parent;

	boolean isRelative;
	float barWidth; // Bar's width in pixels
	float barHeight; // Bar's height in pixels
	float xPosition; // Bar's x position in pixels
	float yPosition; // Bar's y position in pixels
	float sliderPosition, newSliderPosition;
	// Position of slider
	float sliderPositionMin, sliderPositionMax; // Max and min values of slider
	boolean mouseOver;
	boolean locked;

	// Is the mouse over the slider?
	// Is the mouse clicking and dragging the slider now?
	/**
	 * @brief Creates a new horizontal scrollbar
	 * 
	 * @param x
	 *            The x position of the top left corner of the bar in pixels
	 * @param y
	 *            The y position of the top left corner of the bar in pixels
	 * @param w
	 *            The width of the bar in pixels
	 * @param h
	 *            The height of the bar in pixels
	 */
	HScrollbar(PApplet p, float x, float y, float w, float h, boolean isRelative) {
		parent = p;
		barWidth = w;
		barHeight = h;
		xPosition = x;
		yPosition = y;
		this.isRelative = isRelative;
		sliderPosition = xPosition + barWidth / 2 - barHeight / 2;
		newSliderPosition = sliderPosition;
		sliderPositionMin = xPosition;
		sliderPositionMax = xPosition + barWidth - barHeight;
	}

	/**
	 * @brief Updates the state of the scrollbar according to the mouse movement
	 */
	void update() {
		if (isMouseOver()) {
			mouseOver = true;
		} else {
			mouseOver = false;
		}
		if (parent.mousePressed && mouseOver) {
			locked = true;
		}
		if (!parent.mousePressed) {
			locked = false;
		}
		if (locked) {
			if(isRelative)
				newSliderPosition = constrain(parent.mouseX - parent.modelX(xPosition, yPosition, 0) - barHeight / 2,
						sliderPositionMin, sliderPositionMax);
			else 
				newSliderPosition = constrain(parent.mouseX - barHeight / 2,
						sliderPositionMin, sliderPositionMax);
		}
		if (parent.abs(newSliderPosition - sliderPosition) > 1) {
			sliderPosition = sliderPosition
					+ (newSliderPosition - sliderPosition);
		}
	}



	/**
	 * @brief Clamps the value into the interval
	 * 
	 * @param val
	 *            The value to be clamped
	 * @param minVal
	 *            Smallest value possible
	 * @param maxVal
	 *            Largest value possible
	 * 
	 * @return val clamped into the interval [minVal, maxVal]
	 */
	float constrain(float val, float minVal, float maxVal) {
		return parent.min(parent.max(val, minVal), maxVal);
	}

	/**
	 * @brief Gets whether the mouse is hovering the scrollbar
	 * 
	 * @return Whether the mouse is hovering the scrollbar
	 */
	boolean isMouseOver() {
		float x = isRelative ? parent.modelX(xPosition, yPosition, 0) : xPosition;
		float y = isRelative ? parent.modelY(xPosition, yPosition, 0) : yPosition;
		if (parent.mouseX > x && parent.mouseX < x + barWidth
				&& parent.mouseY > y && parent.mouseY < y + barHeight) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @brief Draws the scrollbar in its current state
	 */
	void display() {
		parent.noStroke();
		parent.fill(204);
		parent.rect(xPosition, yPosition, barWidth, barHeight);
		if (mouseOver || locked) {
			parent.fill(0, 0, 0);
		} else {
			parent.fill(102, 102, 102);
		}
		parent.rect(sliderPosition, yPosition, barHeight, barHeight);
	}

	/**
	 * @brief Gets the slider position
	 * 
	 * @return The slider position in the interval [0,1] corresponding to
	 *         [leftmost position, rightmost position]
	 */
	float getPos() {
		return (sliderPosition - xPosition) / (barWidth - barHeight);
	}
}
