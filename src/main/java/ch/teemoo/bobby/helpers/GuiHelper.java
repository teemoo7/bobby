package ch.teemoo.bobby.helpers;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuiHelper {
	private final static Logger logger = LoggerFactory.getLogger(GuiHelper.class);

	private final Font pieceFont = loadPieceFont();
	private final Properties properties = loadProperties();

	public GuiHelper() {
	}

	public Font getPieceFont() {
		return pieceFont;
	}

	public String getVersion() {
		return properties.getProperty("bobby.version");
	}

	public String getBuildTimestamp() {
		return properties.getProperty("bobby.buildTimestamp");
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

	private Properties loadProperties() {
		Properties properties = new Properties();
		try {
			var inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("bobby.properties");
			if (inputStream == null) {
				throw new IOException("Cannot load properties from resources");
			}
			properties.load(inputStream);
		} catch (IOException e) {
			logger.warn("Unable to read properties", e);
		}
		return properties;
	}

}
