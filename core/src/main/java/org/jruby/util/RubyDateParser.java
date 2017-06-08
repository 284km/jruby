package org.jruby.util;

import org.jruby.RubyBignum;
import org.jruby.RubyFixnum;
import org.jruby.RubyRational;
import org.jruby.RubyString;
import org.jruby.runtime.ThreadContext;

import java.util.HashMap;
import java.util.List;

import static org.jruby.util.StrptimeParser.FormatBag.has;

/**
 * This class has {@code StrptimeParser} and provides methods that are calls from JRuby.
 */
public class RubyDateParser {
    private final StrptimeParser strptimeParser;

    public RubyDateParser() {
        this.strptimeParser = new StrptimeParser();
    }

    /**
     * Date._strptime method in JRuby 9.1.5.0's lib/ruby/stdlib/date/format.rb is replaced
     * with this method. This is Java implementation of date__strptime method in MRI 2.3.1's
     * ext/date/date_strptime.c.
     * @see https://github.com/jruby/jruby/blob/036ce39f0476d4bd718e23e64caff36bb50b8dbc/lib/ruby/stdlib/date/format.rb
     * @see https://github.com/ruby/ruby/blob/394fa89c67722d35bdda89f10c7de5c304a5efb1/ext/date/date_strptime.c
     */

    public HashMap<String, Object> parse(ThreadContext context, final RubyString format, final RubyString text) {
        final boolean tainted = text.isTaint();
        final List<StrptimeToken> compiledPattern = strptimeParser.compilePattern(format.asJavaString());
        final StrptimeParser.FormatBag bag = strptimeParser.parse(compiledPattern, text.asJavaString());
        if (bag != null) {
            return convertFormatBagToHash(context, bag, tainted);
        } else {
            return null;
        }
    }

    private HashMap<String, Object> convertFormatBagToHash(ThreadContext context, StrptimeParser.FormatBag bag, boolean tainted) {
        final HashMap<String, Object> map = new HashMap<>();

        if (has(bag.getMDay())) {
            map.put("mday", bag.getMDay());
        }
        if (has(bag.getWDay())) {
            map.put("wday", bag.getWDay());
        }
        if (has(bag.getCWDay())) {
            map.put("cwday", bag.getCWDay());
        }
        if (has(bag.getYDay())) {
            map.put("yday", bag.getYDay());
        }
        if (has(bag.getCWeek())) {
            map.put("cweek", bag.getCWeek());
        }
        if (has(bag.getCWYear())) {
            map.put("cwyear", bag.getCWYear());
        }
        if (has(bag.getMin())) {
            map.put("min", bag.getMin());
        }
        if (has(bag.getMon())) {
            map.put("mon", bag.getMon());
        }
        if (has(bag.getHour())) {
            map.put("hour", bag.getHour());
        }
        if (has(bag.getYear())) {
            map.put("year", bag.getYear());
        }
        if (has(bag.getSec())) {
            map.put("sec", bag.getSec());
        }
        if (has(bag.getWNum0())) {
            map.put("wnum0", bag.getWNum0());
        }
        if (has(bag.getWNum1())) {
            map.put("wnum1", bag.getWNum1());
        }
        if (bag.getZone() != null) {
            final RubyString zone = RubyString.newString(context.getRuntime(), bag.getZone());
            if (tainted) {
                zone.taint(context);
            }
            map.put("zone", zone);
            int offset = TimeZoneConverter.dateZoneToDiff(bag.getZone());
            if (offset != Integer.MIN_VALUE) {
                map.put("offset", offset);
            }
        }
        if (has(bag.getSecFraction())) {
            final RubyBignum secFraction = RubyBignum.newBignum(context.getRuntime(), bag.getSecFraction());
            final RubyFixnum secFractionSize = RubyFixnum.newFixnum(context.getRuntime(), (long)Math.pow(10, bag.getSecFractionSize()));
            map.put("sec_fraction", RubyRational.newRationalCanonicalize(context, secFraction, secFractionSize));
        }
        if (bag.has(bag.getSeconds())) {
            if (has(bag.getSecondsSize())) {
                final RubyBignum seconds = RubyBignum.newBignum(context.getRuntime(), bag.getSeconds());
                final RubyFixnum secondsSize = RubyFixnum.newFixnum(context.getRuntime(), (long)Math.pow(10, bag.getSecondsSize()));
                map.put("seconds", RubyRational.newRationalCanonicalize(context, seconds, secondsSize));
            } else {
                map.put("seconds", bag.getSeconds());
            }
        }
        if (has(bag.getMerid())) {
            map.put("_merid", bag.getMerid());
        }
        if (has(bag.getCent())) {
            map.put("_cent", bag.getCent());
        }
        if (bag.getLeftover() != null) {
            final RubyString leftover = RubyString.newString(context.getRuntime(), bag.getLeftover());
            if (tainted) {
                leftover.taint(context);
            }
            map.put("leftover", leftover);
        }

        return map;
    }
}
