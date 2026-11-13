package perondi.BeShuffle.dtos.draws;

import perondi.BeShuffle.services.Plano;

import java.time.Instant;

/** Quantos sorteios a pessoa ainda tem hoje e quando as fichas voltam. Resposta de GET /api/draws/allowance. */
public record SaldoDeFichas(Plano plan, int remaining, int dailyLimit, Instant resetsAt) {
}
