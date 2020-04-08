package blusunrize.lib.manual;

import blusunrize.lib.manual.gui.ManualScreen;
import net.minecraft.client.gui.widget.button.Button;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class TextSplitterTest
{
	private static final int LINE_LENGTH = 10;
	private static final int LINES_PER_PAGE = 3;
	private static final SpecialManualElement HEIGHT_1 = new DummyElement(1);
	private static final SpecialManualElement HEIGHT_10 = new DummyElement(10);
	private TextSplitter splitter;

	@Before
	public void createSplitter()
	{
		splitter = new TextSplitter(String::length, LINE_LENGTH, LINES_PER_PAGE, () -> 1, s -> s);
	}

	private void assertLineCounts(int... lineCounts)
	{
		List<List<String>> output = splitter.getEntryText();
		int[] actualSizes = output.stream().mapToInt(List::size).toArray();
		Assert.assertArrayEquals("Split result: "+output, lineCounts, actualSizes);
	}

	private void assertLines(String... lines)
	{
		List<List<String>> output = splitter.getEntryText();
		String[] actualLines = output.stream().flatMap(List::stream).toArray(String[]::new);
		Assert.assertArrayEquals("Split result: "+output, lines, actualLines);
	}

	private void assertSpecialAt(int page, SpecialManualElement expected)
	{
		Assert.assertEquals(splitter.getSpecials().toString(), expected, splitter.getSpecials().get(page));
	}

	@Test
	public void testBasicSplitting()
	{
		splitter.split("12345 123456\nabc defghi ABC");
		assertLineCounts(3, 1);
		assertLines("12345", "123456", "abc defghi", "ABC");
	}

	@Test
	public void testLongWord()
	{
		splitter.split("abc aaaaaaa 0123456789 0123456789ABC");
		assertLineCounts(3, 1);
		assertLines("abc", "aaaaaaa", "0123456789", "0123456789ABC");
	}

	@Test
	public void testSpecialElementAtStart()
	{
		splitter.addSpecialPage("start", 0, HEIGHT_1);
		splitter.split("abc def "+"ghi jkl "+"mno");
		assertLineCounts(2, 1);
		assertSpecialAt(0, HEIGHT_1);
		assertLines("abc def", "ghi jkl", "mno");
	}

	@Test
	public void testSpecialElementAtEnd()
	{
		splitter.addSpecialPage("test", 0, HEIGHT_1);
		splitter.split("abc def "+"ghi jkl "+"mno <&test>");
		assertLineCounts(3, 0);
		assertSpecialAt(1, HEIGHT_1);
		assertLines("abc def", "ghi jkl", "mno");
	}

	@Test
	public void testSpecialElementNearEnd()
	{
		splitter.addSpecialPage("test", 0, HEIGHT_1);
		splitter.split("abc def "+"ghi jkl "+"<&test>mno");
		assertLineCounts(2, 1);
		assertSpecialAt(1, HEIGHT_1);
		assertLines("abc def", "ghi jkl", "mno");
	}

	@Test
	public void testLongSpecialElement()
	{
		splitter.addSpecialPage("test", 0, HEIGHT_10);
		splitter.split("abc <&test> def");
		assertLineCounts(1, 0, 1);
		assertSpecialAt(1, HEIGHT_10);
		assertLines("abc", "def");
	}

	@Test
	public void testLongSpecialElementAtEnd()
	{
		splitter.addSpecialPage("test", 0, HEIGHT_10);
		splitter.addSpecialPage("test", 1, HEIGHT_1);
		splitter.split("abc def <&test>");
		assertSpecialAt(1, HEIGHT_10);
		assertSpecialAt(2, HEIGHT_1);
		assertLineCounts(1, 0, 0);
		assertLines("abc def");
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
		public void render(ManualScreen gui, int x, int y, int mouseX, int mouseY)
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
	}
}