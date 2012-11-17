package com.alix;

/**
 * @author Alix Studios
 * 
 * 
 */
public class SpriteExtractor {
	public static void main(String[] args) throws Exception {
		System.setProperty("com.apple.mrj.application.apple.menu.about.name",
				"Sprite Extractor");
		ExtractorUI spriteExtractor = new ExtractorUI();
		spriteExtractor.setVisible(true);
	}

}
