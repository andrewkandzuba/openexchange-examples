package io.openexchange.services;

import io.openexchange.pojos.pushwoosh.Row;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RowsReply extends Reply {
    private final List<Row> rows = new ArrayList<>();

    public RowsReply(int code, String status, List<Row> rows) {
        super(code, status);
        this.rows.addAll(rows);
    }

    public List<Row> getRows() {
        return Collections.unmodifiableList(rows);
    }
}
