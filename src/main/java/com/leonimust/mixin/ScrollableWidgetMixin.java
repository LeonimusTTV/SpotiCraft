package com.leonimust.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ScrollableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ScrollableWidget.class)
public interface ScrollableWidgetMixin {
    @Invoker("drawScrollbar")
    void invokeDrawScrollbar(DrawContext context);
}