/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.world.continent;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Populator;
import com.terraforged.n2d.func.Interpolation;
import com.terraforged.n2d.util.NoiseUtil;

public class ContinentLerper2 implements Populator {

    private final Populator lower;
    private final Populator upper;
    private final Interpolation interpolation;

    private final float blendLower;
    private final float blendUpper;
    private final float blendRange;

    public ContinentLerper2(Populator lower, Populator upper, float min, float max) {
        this(lower, upper, min, max, Interpolation.LINEAR);
    }

    public ContinentLerper2(Populator lower, Populator upper, float min, float max, Interpolation interpolation) {
        this.lower = lower;
        this.upper = upper;
        this.interpolation = interpolation;
        this.blendLower = min;
        this.blendUpper = max;
        this.blendRange = blendUpper - blendLower;
    }

    @Override
    public void apply(Cell cell, float x, float y) {
        if (cell.continentEdge < blendLower) {
            lower.apply(cell, x, y);
            return;
        }

        if (cell.continentEdge > blendUpper) {
            upper.apply(cell, x, y);
            return;
        }

        float alpha = interpolation.apply((cell.continentEdge - blendLower) / blendRange);
        lower.apply(cell, x, y);

        float lowerVal = cell.value;

        upper.apply(cell, x, y);
        float upperVal = cell.value;

        cell.value = NoiseUtil.lerp(lowerVal, upperVal, alpha);
    }
}
