/*
 *  Copyright 2018 jd.com
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yihaodian.architecture.kira.manager.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class VerifyImg {

  public static Map createVerifyImg() {
    Map reMap = new HashMap();
    int width = 70;
    int height = 30;
    BufferedImage buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = buffImg.createGraphics();
    Random random = new Random();
    g.setColor(VerifyImg.getRandColor(200, 250));
    g.fillRect(0, 0, width, height);
    Font font = new Font("Times New Roman", Font.HANGING_BASELINE, 28);
    g.setFont(font);
    g.setColor(Color.BLACK);
    g.drawRect(0, 0, width - 1, height - 1);
    // g.setColor(Color.GRAY);
    g.setColor(VerifyImg.getRandColor(160, 200));
    for (int i = 0; i < 155; i++) {
      int x = random.nextInt(width);
      int y = random.nextInt(height);
      int xl = random.nextInt(12);
      int yl = random.nextInt(12);
      g.drawLine(x, y, x + xl, y + yl);
    }
    StringBuffer randomCode = new StringBuffer();
    int length = 4;
    String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    int size = base.length();
    for (int i = 0; i < length; i++) {
      int start = random.nextInt(size);
      String strRand = base.substring(start, start + 1);
      // g.setColor(getRandColor(1, 100));
      g.setColor(
          new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
      g.drawString(strRand, 15 * i + 6, 24);
      randomCode.append(strRand);
    }
    reMap.put("code", randomCode.toString());
    reMap.put("files", buffImg);
    g.dispose();
    return reMap;
  }

  public static Color getRandColor(int fc, int bc) {
    Random random = new Random();
    if (fc > 255) {
      fc = 255;
    }
    if (bc > 255) {
      bc = 255;
    }
    int r = fc + random.nextInt(bc - fc);
    int g = fc + random.nextInt(bc - fc);
    int b = fc + random.nextInt(bc - fc);
    return new Color(r, g, b);
  }

}
