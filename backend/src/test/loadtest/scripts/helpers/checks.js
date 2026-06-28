import { check } from 'k6';

export const checkHttp = (res, status) => {
    check(res, {
        'correct status':      (r) => r.status === status,
        'body no vacio':   (r) => r.body.length > 0,
    });
}

export const checkPaginacion = (elems) => {

    check(elems, {
        'esta paginado': (i) => i.numero != null && typeof i.numero === 'number',
        'contenido es array': (i) => Array.isArray(i.contenido),
        'tiene elementos': (i) => i.contenido.length > 0,
    });
}