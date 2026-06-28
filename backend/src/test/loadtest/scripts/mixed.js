import { sleep } from 'k6';
import { login } from './helpers/auth.js';
import { optionsDefault } from './helpers/options.js';
import { testFiguritas }    from './figuritas.js';
import { testMisFaltantes } from './mis-faltantes.js';
import { testIntercambios } from './intercambios.js';
import { testSubastas }     from './subastas.js';
import { testSugerencias }  from './sugerencias.js';

export const options = optionsDefault;

const BASE = 'http://backend-test:8080';
const USUARIO = { nombre: 'lucas_fis', contrasenia: 'Gordo123!' };

const RUTAS = [
    { peso: 35, fn: testFiguritas      },  // la más consultada
    { peso: 25, fn: testMisFaltantes   },
    { peso: 20, fn: testIntercambios   },
    { peso: 15, fn: testSubastas       },
    { peso: 5,  fn: testSugerencias    },  // más pesada, menos frecuente
];

// Precalcula los rangos acumulados una sola vez
const RANGOS = RUTAS.reduce((acc, ruta) => {
    const ultimo = acc.length > 0 ? acc[acc.length - 1].hasta : 0;
    acc.push({ hasta: ultimo + ruta.peso, fn: ruta.fn });
    return acc;
}, []);

const TOTAL_PESO = RANGOS[RANGOS.length - 1].hasta;

function elegirRuta() {
    const n = Math.random() * TOTAL_PESO;
    return RANGOS.find(r => n < r.hasta).fn;
}

export default function () {
    const cookie = login(BASE, USUARIO);
    const authHeaders = { 'Cookie': `token=${cookie}` };

    const ruta = elegirRuta();
    ruta(authHeaders);

    sleep(1);
}