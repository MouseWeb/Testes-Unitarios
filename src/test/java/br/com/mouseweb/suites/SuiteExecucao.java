package br.com.mouseweb.suites;

import br.com.mouseweb.service.CalculadoraTest;
import br.com.mouseweb.service.CalculoValorLocacaoTest;
import br.com.mouseweb.service.LocacaoServiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

//@RunWith(Suite.class)
@SuiteClasses({
        CalculadoraTest.class,
        CalculoValorLocacaoTest.class,
        LocacaoServiceTest.class
})
public class SuiteExecucao {
    //Remova se puder!
}
