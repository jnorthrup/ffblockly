package ffblockly.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class AvOptionBuilder {
    private Matcher optMatcher;

    public AvOptionBuilder setOptMatcher(Matcher optMatcher) {
        this.optMatcher = optMatcher;
        return this;
    }

    public AvOption  createAvOptionImpl() {
        final Matcher optMatcher1 = optMatcher;
        return Scan.MYFACTORY.options(new AvOption() {
            private final Matcher optMatcher = optMatcher1;
            protected List<AvOption> ch;
            private List<AvTarget> r;

            @Override
            public String getName() {
                return this.optMatcher.group(AvOptionField.name.name());
            }

            @Override
            public String getType() {
                return this.optMatcher.group(AvOptionField.type.name());
            }

            @Override
            public List<AvTarget> getTargets() {
                if (this.getType() != null)
                    if (null == this.r) {
                        this.r = new ArrayList<>();
                        String group = this.optMatcher.group(AvOptionField.targets.name());
                        for (AvTarget avTarget : AvTarget.values())
                            if ('.' != group.charAt(avTarget.ordinal()))
                                this.r.add(avTarget);
                    }
                return this.r;
            }

            @Override
            public String getDescription() {
                return this.optMatcher.group(AvOptionField.description.name());
            }

            @Override
            public String getRange1() {
                return this.optMatcher.group(AvOptionField.range1.name());
            }

            @Override
            public String getRange2() {
                return this.optMatcher.group(AvOptionField.range2.name());
            }

            @Override
            public String getDef() {
                return this.optMatcher.group(AvOptionField.def.name());
            }

            @Override
            public Boolean getEncoding() {
                return null != this.getType() ? this.getTargets().contains(AvTarget.Encoding) ? true : null : null;
            }

            @Override
            public Boolean getDecoding() {
                return null != this.getType() ? this.getTargets().contains(AvTarget.Decoding) ? true : null : null;
            }

            @Override
            public Boolean getFiltering() {
                return null != this.getType() ? this.getTargets().contains(AvTarget.Filtering) ? true : null : null;

            }

            @Override
            public Boolean getVideo() {
                return null != this.getType() ? this.getTargets().contains(AvTarget.Video) ? true : null : null;

            }

            @Override
            public Boolean getAudio() {
                return null != this.getType() ? this.getTargets().contains(AvTarget.Audio) ? true : null : null;

            }

            @Override
            public Boolean getSubtitle() {
                return null != this.getType() ? this.getTargets().contains(AvTarget.Subtitle) ? true : null : null;

            }

            @Override
            public Boolean getExport() {
                return null != this.getType() ? this.getTargets().contains(AvTarget.Export) ? true : null : null;

            }

            @Override
            public Boolean getReadonly() {
                return null != this.getType() ? this.getTargets().contains(AvTarget.Readonly) ? true : null : null;
            }

            @Override
            public List<AvOption> getChildren() {
                return this.ch;
            }

            @Override
            public void addChild(AvOption o) {
                if (this.ch == null)
                    this.ch = new ArrayList<>();
                this.ch.add(o);
            }
        }).as();
    }
}