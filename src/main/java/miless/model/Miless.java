package miless.model;


import miless.model.auto._Miless;

public class Miless extends _Miless {

    private static Miless instance;

    private Miless() {}

    public static Miless getInstance() {
        if(instance == null) {
            instance = new Miless();
        }

        return instance;
    }
}
