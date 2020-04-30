package ch.teemoo.bobby.helpers;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuiHelper {
	private final static Logger logger = LoggerFactory.getLogger(GuiHelper.class);

	private final Font pieceFont = loadPieceFont();

	public GuiHelper() {
	}

	public Font getPieceFont() {
		return pieceFont;
	}

	private Font loadPieceFont() {
		try {
			var inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("fonts/FreeSerif.ttf");
			if (inputStream == null) {
				throw new IOException("Cannot load font from resource");
			}
			var font = Font.createFont(Font.TRUETYPE_FONT, inputStream);
			return font.deriveFont(Font.PLAIN, 72);
		} catch (IOException | FontFormatException e) {
			logger.warn("Unable to use embedded font, using fallback", e);
			return new Font("Serif", Font.PLAIN, 48);
		}
	}

}
