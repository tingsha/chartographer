CREATE TABLE IF NOT EXISTS Canvas(
    id SERIAL,
    width INTEGER NOT NULL,
    height INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS Fragment(
    id SERIAL,
    canvas_id INTEGER,
    x INTEGER,
    y INTEGER,
    width INTEGER NOT NULL,
    height INTEGER NOT NULL,
    date TIMESTAMP DEFAULT now(),
    FOREIGN KEY (canvas_id) REFERENCES Canvas (id) ON DELETE CASCADE
);