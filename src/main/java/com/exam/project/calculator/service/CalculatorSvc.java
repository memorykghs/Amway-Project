package com.exam.project.calculator.service;

import com.exam.project.calculator.controller.dto.CalculatorReq;
import com.exam.project.calculator.enums.BiasEnum;
import com.exam.project.calculator.enums.OperateEnum;
import com.exam.project.calculator.exception.CalculateException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author memorykghs
 * @date 2024/12/7
 */
@Service
@Slf4j
public class CalculatorSvc {

    private final Stack<BigDecimal> undoStack = new Stack<>();

    private final Stack<BigDecimal> redoStack = new Stack<>();

    private BigDecimal currentResult = BigDecimal.ZERO;

    private final Map<String, IOperation> serviceMap; // (operators, service)

    public CalculatorSvc(List<IOperation> serviceList) {
        serviceMap = serviceList.stream()
                .collect(Collectors.toMap(e -> e.getOperateEnum().getOperator(), Function.identity()));
        undoStack.push(BigDecimal.ZERO); // 初始化計算機
    }

    /**
     * 計算
     *
     * @param calculatorDTO
     * @return
     */
    @SneakyThrows
    public BigDecimal calculate(CalculatorReq calculatorDTO, OperateEnum operateEnum) {
        String operator = operateEnum.getOperator();
        IOperation operationService = serviceMap.get(operator);
        if (operationService == null) {
            log.error("This operation haven't been implement: {} ", operator);
            throw new CalculateException("This operation haven't been implement.");
        }

        BigDecimal num1 = calculatorDTO.getNum1();
        BigDecimal num2 = calculatorDTO.getNum2();

        log.info("calculate data: {} {} {}", num1, operator, num2);
        currentResult = serviceMap.get(operator).operate(calculatorDTO.getNum1(), calculatorDTO.getNum2());
        updateUndoState(); // 更新計算結果
        return currentResult;
    }

    /**
     * 有誤差（bias）的計算
     *
     * @param operator
     * @param num1
     * @param num2
     * @param biasEnum
     * @return
     */
    public BigDecimal calculateWithBias(String operator, BigDecimal num1, BigDecimal num2, BiasEnum biasEnum) {
        currentResult = serviceMap.get(operator).operateWithBias(num1, num2, biasEnum);
        return currentResult;
    }

    /**
     * 復原
     *
     * @return
     */
    @SneakyThrows
    public BigDecimal undo() {
        if (undoStack.isEmpty()) {
            throw new CalculateException("There is no undo history");
        }
        redoStack.push(undoStack.pop());
        currentResult = undoStack.peek();
        return currentResult;
    }

    /**
     * 取消復原
     *
     * @return
     */
    @SneakyThrows
    public BigDecimal redo() {
        if (redoStack.isEmpty()) {
            throw new CalculateException("There is no redo history");
        }
        BigDecimal redo = undoStack.peek();
        currentResult = redoStack.pop();
        undoStack.push(currentResult);
        redoStack.push(redo);
        return currentResult;
    }

    public void reset() {
        currentResult = BigDecimal.ZERO;
        undoStack.clear();
        undoStack.push(currentResult);
        redoStack.clear();
    }

    /**
     * 更新最後一次操作記錄到 undo stack
     * 並清空 redo stack
     */
    private void updateUndoState() {
        undoStack.push(currentResult);
        redoStack.clear();
    }
}
