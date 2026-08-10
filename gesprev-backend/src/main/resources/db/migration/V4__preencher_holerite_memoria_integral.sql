UPDATE memoria_calculo memoria
SET holerite_id = (
    SELECT holerite.id
    FROM holerite
    INNER JOIN documento ON documento.id = holerite.id
    WHERE documento.processo_id = memoria.processo_id
    ORDER BY TO_DATE(holerite.mes_referencia, 'MM/YYYY') DESC NULLS LAST,
             documento.dt_upload DESC
    LIMIT 1
)
WHERE memoria.tipo_calculo = 'INTEGRAL'
  AND memoria.holerite_id IS NULL;
