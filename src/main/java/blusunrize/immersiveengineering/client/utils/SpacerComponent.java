/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.utils;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.util.Lazy;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public final class SpacerComponent implements Component
{
	private final String widthGlyph;
	private final List<Component> siblings = Lists.newArrayList();
	private final Lazy<FormattedCharSequence> formatted = Lazy.of(() -> Language.getInstance().getVisualOrder(this));

	/** This component is not serializable! It can only be used in a client context, for rendering!
	 */
	public SpacerComponent(String widthGlyph)
	{
		this.widthGlyph = widthGlyph;
	}

	public SpacerComponent append(Component sibling)
	{
		this.siblings.add(sibling);
		return this;
	}

	@Nonnull
	@Override
	public Style getStyle()
	{
		return Style.EMPTY;
	}

	@Nonnull
	@Override
	public ComponentContents getContents()
	{
		return PlainTextContents.EMPTY;
	}

	@Override
	public List<Component> getSiblings()
	{
		return siblings;
	}

	@Override
	public FormattedCharSequence getVisualOrderText()
	{
		return formatted.get();
	}

	public int getSpaceWidth(Font font)
	{
		return font.width(widthGlyph);
	}

	@Override
	public boolean equals(Object other)
	{
		if(this==other)
			return true;
		if(other instanceof SpacerComponent otherSpacer)
			return this.widthGlyph.equals(otherSpacer.widthGlyph)&&this.siblings.equals(otherSpacer.siblings);
		return false;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(widthGlyph, siblings);
	}

	@Override
	public String toString()
	{
		return "SpacerComponent["+
				"widthGlyph="+widthGlyph+", "+
				"siblings="+siblings+']';
	}

}
