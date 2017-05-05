package com.hp.jipp.model;

import com.google.auto.value.AutoValue;
import com.hp.jipp.encoding.NameCodeEncoder;
import com.hp.jipp.encoding.NameCode;

/** An enumeration of possible printer states */
@AutoValue
public abstract class PrinterState extends NameCode {

    public static final PrinterState Idle = of("idle", 3);
    public static final PrinterState Processing = of("processing", 4);
    public static final PrinterState Stopped = of("stopped", 5);

    public static final NameCodeEncoder<PrinterState> ENCODER = NameCodeEncoder.of(
            PrinterState.class, new NameCode.Factory<PrinterState>() {
                @Override
                public PrinterState of(String name, int code) {
                    return PrinterState.of(name, code);
                }
            });

    public static PrinterState of(String name, int code) {
        return new AutoValue_PrinterState(name, code);
    }
}
