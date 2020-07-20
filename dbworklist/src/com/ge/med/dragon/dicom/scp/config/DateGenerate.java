/**
 * DateGenerate.java 1.0 2013-3-4
 *
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.scp.config;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import com.ge.med.dragon.rispacs.utils.DateUtil;

/**
 * The Class DateGenerate.
 */
public class DateGenerate implements GenerateValue {
    private SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATE_FORMAT);

    /**
     * {@inheritDoc. Override}
     */
    public String generate(String root) throws Exception {
        if (root.equals("TODAY")) {
            return sdf.format(new Date());
        } else if (root.equals("YESTERDAY")) {
            GregorianCalendar gc = new GregorianCalendar();
            gc.add(GregorianCalendar.DATE, -1);
            Date oneDay = gc.getTime();
            return sdf.format(oneDay);
        } else if (root.equals("TDBYESTERDAY")) {
            GregorianCalendar gc = new GregorianCalendar();
            gc.add(GregorianCalendar.DATE, -2);
            Date oneDay = gc.getTime();
            return sdf.format(oneDay);
        } else if (root.equals("TWODAYS")) {
            GregorianCalendar gc = new GregorianCalendar();
            Date current = gc.getTime();
            gc.add(GregorianCalendar.DATE, -1);
            Date oneDay = gc.getTime();
            return sdf.format(oneDay) + "^" + sdf.format(current);
        } else if (root.equals("ONEWEEK")) {
            GregorianCalendar gc = new GregorianCalendar();
            Date current = gc.getTime();
            gc.add(GregorianCalendar.DATE, -7);
            Date oneDay = gc.getTime();
            return sdf.format(oneDay) + "^" + sdf.format(current);
        } else if (root.equals("ONEMONTH")) {
            GregorianCalendar gc = new GregorianCalendar();
            Date current = gc.getTime();
            gc.add(GregorianCalendar.MONTH, -1);
            Date oneDay = gc.getTime();
            return sdf.format(oneDay) + "^" + sdf.format(current);
        } else {
            StringTokenizer st = new StringTokenizer(root, "^");
            while (st.hasMoreTokens()) {
                String temp = st.nextToken();
                sdf.parse(temp);
            }
            return root;
        }
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String args[]) {
        try {
            System.out.println(new SimpleDateFormat(DateUtil.DATE_FORMAT).format(new SimpleDateFormat("yyyyMMdd")
                    .parse(new DateGenerate().generate("TODAY"))));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}


/******************************************************************************
 * Revision History 
 * [type 'revision' & press Alt + '/' to insert revision block]
 *
 *
 ******************************************************************************/
