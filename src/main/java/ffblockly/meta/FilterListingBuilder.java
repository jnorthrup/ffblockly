package ffblockly.meta;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

public interface FilterListingBuilder {

    static List<FilterListing.FilterSignature> getFilterSignatures(String group) {
        List<FilterListing.FilterSignature> filterSignatures = new ArrayList<>();
        StringReader stringReader = new StringReader(group);
        int b;
        try {
            while (-1 != (b = stringReader.read())) {

                FilterListing.FilterSignature e = FilterListing.FilterSignature.valueOf((char) (b & 0xffff));
                filterSignatures.add(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filterSignatures.isEmpty() ? null : filterSignatures;
    }


    static FilterListing createFilterListing(final Matcher filterMatcher1) {
         return Scan.MYFACTORY.listing(new FilterListing() {
            private final Matcher filterMatcher = filterMatcher1;
            private List<AvOption> opts = null;

            @Override
            public Boolean getTimelineSupport() {
                return Boolean.valueOf(!Objects.equals(".", filterMatcher.group(Attr.timelineSupport.ordinal() + 1))) ? true : null;
            }

            @Override
            public Boolean getSliceThreading() {
                return Boolean.valueOf(!Objects.equals(".", filterMatcher.group(Attr.sliceThreading.ordinal() + 1))) ? true : null;
            }

            @Override
            public Boolean getCommandSupport() {
                return Boolean.valueOf(!Objects.equals(".", filterMatcher.group(Attr.commandSupport.ordinal() + 1))) ? true : null;
            }

            @Override
            public String getFilterName() {
                return (filterMatcher.group(Attr.filterName.ordinal() + 1));
            }

            @Override
            public String getShortDescription() {
                return filterMatcher.group(Attr.shortDescription.ordinal() + 1);
            }

            @Override
            public List<FilterSignature> getInputSignature() {
                String group = filterMatcher.group(Attr.inputSignature.ordinal() + 1);
                return getFilterSignatures(group);
            }

            @Override
            public List<FilterSignature> getReturnSignature() {
                String group = filterMatcher.group(Attr.returnSignature.ordinal() + 1);
                return getFilterSignatures(group);

            }

            @Override
            public List<AvOption> getOptions() {
                if (null == opts) {
                    Process process = null;
                    try {
                        process = new ProcessBuilder("ffmpeg", "-h", "filter=" + getFilterName()).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String buf;
                        AvOption prev = null;
                        while (null != (buf = bufferedReader.readLine())) {
                            Matcher optMatcher = AvOption.AV_OPT_PAT.matcher(buf);
                            boolean b = optMatcher.matches();

                            if (b) {
                                AvOption avOption = new AvOptionBuilder().setOptMatcher(optMatcher).createAvOptionImpl();

                                String type = avOption.getType();
                                if (type != null && !type.isEmpty()) {
                                    prev = avOption;
                                    if (null == opts) opts = new ArrayList<>();

                                    opts.add(avOption);
                                } else {
                                    if (prev == null)
                                        prev = avOption;
                                    else {
                                        prev.addChild(avOption);
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        process.destroy();
                    }
                }
                return opts;
            }
        }).as();
    }
}