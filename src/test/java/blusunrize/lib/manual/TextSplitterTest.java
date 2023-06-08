/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.SplitResult.Token;
import blusunrize.lib.manual.gui.ManualScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class TextSplitterTest
{
	private static final int LINE_LENGTH = 10;
	private static final int LINES_PER_PAGE = 3;
	private static final String ZERO_WIDTH_TEXT = "\u00a7b";
	private static final SpecialManualElement HEIGHT_1 = new DummyElement(1);
	private static final SpecialManualElement HEIGHT_1_B = new DummyElement(1);
	private static final SpecialManualElement HEIGHT_10 = new DummyElement(10);
	private TextSplitter splitter;

	@Before
	public void createSplitter()
	{
		splitter = new TextSplitter(
				s -> s.replace(ZERO_WIDTH_TEXT, "").length(),
				LINE_LENGTH,
				LINES_PER_PAGE,
				() -> 1,
				s -> s
		);
	}

	private void assertLineCounts(SplitResult result, int... lineCounts)
	{
		List<List<List<Token>>> output = result.entry;
		int[] actualSizes = output.stream().mapToInt(List::size).toArray();
		Assert.assertArrayEquals("Split result: "+output, lineCounts, actualSizes);
	}

	private void assertLines(SplitResult result, String... lines)
	{
		List<List<List<Token>>> output = result.entry;
		String[] actualLines = output.stream()
				.flatMap(List::stream)
				.map(l -> l.stream().map(Token::getText).collect(Collectors.joining()))
				.toArray(String[]::new);
		Assert.assertArrayEquals("Split result: "+output, lines, actualLines);
	}

	private void assertSpecialAt(SplitResult result, int page, SpecialManualElement expected)
	{
		Assert.assertEquals(result.specialByPage.toString(), expected, result.specialByPage.get(page));
	}

	@Test
	public void testBasicSplitting()
	{
		SplitResult result = splitter.split("12345 123456\nabc defghi ABC");
		assertLineCounts(result, 3, 1);
		assertLines(result, "12345", "123456", "abc defghi", "ABC");
	}

	@Test
	public void testLongWord()
	{
		SplitResult result = splitter.split("abc aaaaaaa 0123456789 0123456789ABC");
		assertLineCounts(result, 3, 2);
		assertLines(result, "abc", "aaaaaaa", "0123456789", "0123456789", "ABC");
	}

	@Test
	public void testSpecialElementAtStart()
	{
		splitter.addSpecialPage("start", 0, HEIGHT_1);
		SplitResult result = splitter.split("abc def "+"ghi jkl "+"mno");
		assertLineCounts(result, 2, 1);
		assertSpecialAt(result, 0, HEIGHT_1);
		assertLines(result, "abc def", "ghi jkl", "mno");
	}

	@Test
	public void testSpecialElementAtEnd()
	{
		splitter.addSpecialPage("test", 0, HEIGHT_1);
		SplitResult result = splitter.split("abc def "+"ghi jkl "+"mno <&test>");
		assertLineCounts(result, 3, 0);
		assertSpecialAt(result, 1, HEIGHT_1);
		assertLines(result, "abc def", "ghi jkl", "mno");
	}

	@Test
	public void testSpecialElementNearEnd()
	{
		splitter.addSpecialPage("test", 0, HEIGHT_1);
		SplitResult result = splitter.split("abc def "+"ghi jkl "+"<&test>mno");
		assertLineCounts(result, 2, 1);
		assertSpecialAt(result, 1, HEIGHT_1);
		assertLines(result, "abc def", "ghi jkl", "mno");
	}

	@Test
	public void testLongSpecialElement()
	{
		splitter.addSpecialPage("test", 0, HEIGHT_10);
		SplitResult result = splitter.split("abc <&test> def");
		assertLineCounts(result, 1, 0, 1);
		assertSpecialAt(result, 1, HEIGHT_10);
		assertLines(result, "abc", "def");
	}

	@Test
	public void testLongSpecialElementAtEnd()
	{
		splitter.addSpecialPage("test", 0, HEIGHT_10);
		splitter.addSpecialPage("test", 1, HEIGHT_1);
		SplitResult result = splitter.split("abc def <&test>");
		assertSpecialAt(result, 1, HEIGHT_10);
		assertSpecialAt(result, 2, HEIGHT_1);
		assertLineCounts(result, 1, 0, 0);
		assertLines(result, "abc def");
	}

	@Test
	public void testLongWordAfterZeroWidth()
	{
		final String longWordA = "0123456789";
		final String longWordB = "ABCDEF";
		SplitResult result = splitter.split(ZERO_WIDTH_TEXT+" "+longWordA+longWordB);
		assertLineCounts(result, 2);
		assertLines(result, ZERO_WIDTH_TEXT+longWordA, ZERO_WIDTH_TEXT+longWordB);
	}

	@Test
	public void testSuccessiveAnchors()
	{
		splitter.addSpecialPage("test_a", 0, HEIGHT_1);
		splitter.addSpecialPage("test_b", 0, HEIGHT_1_B);
		SplitResult result = splitter.split("<&test_a><&test_b>test");
		assertSpecialAt(result, 0, HEIGHT_1);
		assertSpecialAt(result, 1, HEIGHT_1_B);
		assertLineCounts(result, 0, 1);
		assertLines(result, "test");
	}

	@Test
	public void testExactLine()
	{
		var firstLine = "0123456789";
		var secondLine = "abc";
		var result = splitter.split(firstLine+"\n"+secondLine);
		assertLineCounts(result, 2);
		assertLines(result, firstLine, secondLine);
	}

	private static class DummyElement extends SpecialManualElement
	{
		private final int lines;

		private DummyElement(int lines)
		{
			this.lines = lines;
		}

		@Override
		public int getPixelsTaken()
		{
			return lines;
		}

		@Override
		public void onOpened(ManualScreen gui, int x, int y, List<Button> buttons)
		{
		}

		@Override
		public void render(GuiGraphics graphics, ManualScreen gui, int x, int y, int mouseX, int mouseY)
		{
		}

		@Override
		public void mouseDragged(int x, int y, double clickX, double clickY, double mx, double my, double lastX, double lastY, int mouseButton)
		{
		}

		@Override
		public boolean listForSearch(String searchTag)
		{
			return false;
		}

		@Override
		public void recalculateCraftingRecipes()
		{
		}

		@Override
		public String toString()
		{
			return "Dummy elements with "+this.lines+" lines";
		}
	}
}