package util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class FormateadorMoneda {

    private static final Locale LOCALE_ARGENTINA = new Locale("es", "AR");

    private FormateadorMoneda() {
    }

    public static String pesos(BigDecimal valor) {
        BigDecimal importe = valor == null ? BigDecimal.ZERO : valor;
        importe = importe.setScale(2, RoundingMode.HALF_UP);

        DecimalFormatSymbols simbolos = DecimalFormatSymbols.getInstance(
                LOCALE_ARGENTINA);
        DecimalFormat formato = new DecimalFormat("#,##0.##", simbolos);
        formato.setRoundingMode(RoundingMode.HALF_UP);
        formato.setMinimumFractionDigits(
                importe.stripTrailingZeros().scale() > 0 ? 2 : 0);
        formato.setMaximumFractionDigits(2);

        return "ARS " + formato.format(importe);
    }
}
