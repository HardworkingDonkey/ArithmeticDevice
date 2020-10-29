package com.donkey;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 刻苦驴
 * @package com.donkey
 * @description 四则运算生成器
 * @since 2020.10.29 16:35
 */

// 请用Java语言编写一个四则运算式子生成器, 可以随机生成加、减、乘、除的式子, 支持随机括号
// 式子中包含的运算符个数和种类由用户选择, 一次生成的式子数量由用户选择
// 生成的式子以文本文件或HTML文件保存, 正确答案保存在另外一个文件中
// 可以自由决定使用命令行程序或图形用户界面程序
public class ArithmeticDevice {
    static boolean hasPlus;
    static boolean hasMinus;
    static boolean hasMultiply;
    static boolean hasDivide;
    static int operatorCount;
    static int expressionCount = 0;
    static int maxNum = 0;
    static String exercisesPath = "src/exercises.txt";
    static String answerPath = "src/answer.txt";
    static Scanner scanner = new Scanner(System.in);
    static ArrayList<String> operators = new ArrayList<>();

    public static void main(String[] args) {
        // 初始化参数
        initParam();
        OutputStream toExercises = null;
        OutputStream toAnswer = null;
        try {
            // 获取输出流
            toExercises = new FileOutputStream(exercisesPath);
            toAnswer = new FileOutputStream(answerPath);
            for (int i = 0; i < expressionCount; i++) {
                // 生成原始中缀表达式
                String infixStr = generateInfix();
                // 插入括号
                String[] infix = insertBrackets(new StringBuilder(infixStr));
                // 转为后缀表达式
                String[] postfix = infix_to_postfix(infix);
                // 拼接习题字符串
                StringBuilder exercise = new StringBuilder();
                exercise.append(i + 1).append(".  "); // 题号
                for (String s : infix) {
                    exercise.append(s).append(" ");
                }
                // 根据后缀表达式计算结果
                double result = postfixCalculate(postfix);
                // 拼接答案字符串
                StringBuilder answer = new StringBuilder();
                answer.append(i + 1).append(".  ").append(result);
                // 如果不是最后一行就加\n
                if (i == expressionCount - 1) {
                    exercise.append("= ");
                } else {
                    exercise.append("= \n");
                    answer.append("\n");
                }
                toExercises.write(exercise.toString().getBytes());
                toExercises.flush();
                toAnswer.write(answer.toString().getBytes());
                toAnswer.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 关闭输出流
        closeCloseable(toExercises);
        closeCloseable(toAnswer);
    }

    /**
     * 随机生成不带括号的运算式
     *
     * @return java.lang.StringBuilder 运算式
     */
    private static String generateInfix() {
        StringBuilder buffer = new StringBuilder();
        // 生成1到100之间的随机整数
        int random = 1 + (int) (Math.random() * (maxNum - 1));
        // 运算数比运算符多一个, 所以先添加运算数
        buffer.append(random).append(" ");
        // 添加运算符和运算数
        for (int i = 0; i < operatorCount; i++) {
            random = 1 + (int) (Math.random() * (maxNum - 1));
            // 随机添加运算符
            buffer.append(operators.get(random % operators.size())).append(" ");
            buffer.append(random).append(" ");
        }
        return buffer.substring(0, buffer.length() - 1);
    }

    /**
     * 插入括号到中缀表达式中
     *
     * @param infix 中缀表达式
     * @return java.lang.String[] 最终的中缀表达式
     */
    private static String[] insertBrackets(StringBuilder infix) {
        // 括号对数最大值为操作符 - 1
        int bracketCount = (int) (Math.random() * 100 % (operatorCount - 1));
        int curBracketCount = 0;
        // 先添加左括号
        for (int i = 0; curBracketCount < bracketCount && i < bracketCount; i++) {
            // 遍历字符串, 在数字之前添加左括号
            // 还需判断该数字是否是最后一个数, 如果是则不要插入括号, infix.length() - 1 除去最后一个数
            for (int j = 0; curBracketCount < bracketCount && j < infix.length() - 1; ) {
                boolean isDigit = Character.isDigit(infix.charAt(j));
                // 如果是数字
                if (isDigit) {
                    // 随机: 是否要插入左括号
                    boolean flag = (int) (Math.random() * 10 % 2) == 1;
                    if (flag) {
                        // 在数字前插入左括号
                        infix.insert(j, "( ");
                        // 当前括号数+1
                        curBracketCount++;
                        // 后移4位, 找到下一个符号
                        j += 4;
                    }
                } else {
                    j++;
                }
            }
        }
        // 如果前面添加了左括号
        if (curBracketCount != 0) {
            bracketCount = curBracketCount;
            curBracketCount = 0;
            // 再添加右括号, 找到第一个有左括号的数字
            // 第一个右括号至少在第一个左括号的往后一个数字+运算符+数字后
            int first = infix.indexOf("(") == -1 ? -1 : infix.indexOf("(") + 2 + 2 + 2;
            for (int i = first; curBracketCount < bracketCount && i < bracketCount; i++) {
                // 遍历字符串, 在数字之前添加左括号
                // 还需判断该数字是否是最后一个数, 如果是则不要插入括号, infix.length() - 1 除去最后一个数
                for (int j = first; curBracketCount < bracketCount && j < infix.length() - 1; ) {
                    boolean isDigit = Character.isDigit(infix.charAt(j));
                    // 如果是数字
                    if (isDigit) {
                        // 随机: 是否要插入右括号
                        boolean flag = (int) (Math.random() * 10 % 2) == 1;
                        // 并且如果该数字前面不是左括号
                        if (flag && infix.charAt(j - 2) != '(') {
                            // 在数字后插入右括号, StringBuilder允许在索引length()处插入
                            infix.insert(j + 1, " )");
                            // 当前括号数+1
                            curBracketCount++;
                            // 后移4位
                            j += 4;
                        }
                    } else {
                        j++;
                    }
                }
            }
            // 把剩余的所有右括号加到最右
            while (curBracketCount != bracketCount) {
                infix.append(" )");
                curBracketCount++;
            }
            // 如果首尾刚好是左右括号, 直接去掉
            if (infix.charAt(0) == '(' && infix.charAt(infix.length() - 1) == ')') {
                infix.delete(0, 2);
                infix.delete(infix.length() - 2, infix.length());
            }
        }
        return infix.toString().split(" ");
    }

    /**
     * 关闭资源
     *
     * @param closeable 可关闭资源
     * @return void
     */
    private static void closeCloseable(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化参数
     *
     * @return void
     */
    private static void initParam() {
        System.out.print("请输入运算符总个数: ");
        operatorCount = scanner.nextInt();
        System.out.print("是否要加号(输入1表示要): ");
        hasPlus = scanner.nextInt() == 1;
        System.out.print("是否要减号(输入1表示要): ");
        hasMinus = scanner.nextInt() == 1;
        System.out.print("是否要乘号(输入1表示要): ");
        hasMultiply = scanner.nextInt() == 1;
        System.out.print("是否要除号(输入1表示要): ");
        hasDivide = scanner.nextInt() == 1;
        if (hasPlus) {
            operators.add("+");
        }
        if (hasMinus) {
            operators.add("-");
        }
        if (hasMultiply) {
            operators.add("*");
        }
        if (hasDivide) {
            operators.add("/");
        }
        System.out.print("请输入运算式个数: ");
        expressionCount = scanner.nextInt();
        System.out.print("请输入随机数的最大值: ");
        maxNum = scanner.nextInt();
    }

    /**
     * 中缀转后缀
     *
     * @param infix 中缀表达式
     * @return java.lang.String[] 后缀表达式
     */
    private static String[] infix_to_postfix(String[] infix) {
//        String[] postfix = new String[infix.length];
        List<String> postfix = new LinkedList<>();
        Map<String, Integer> priority = new HashMap<>();
        // 初始化运算符优先级
        priority.put(null, 0);
        priority.put("+", 1);
        priority.put("-", 1);
        priority.put("*", 2);
        priority.put("/", 2);
        // 保存运算符和左括号
        LinkedList<String> stack = new LinkedList<>();
        // 扫描中缀表达式
        for (String elem : infix) {
            // 如果遇到左括号, 将其压入栈
            if ("(".equals(elem)) {
                stack.push(elem);
                // 如果遇到运算数, 将其添加到后缀表达式后面
            } else if (isNumeric(elem)) {
                postfix.add(elem);
                // 遇到运算符
            } else if (isOperator(elem)) {
                // 弹出所有和它具有相等或者更高优先级的运算符
                while (!"(".equals(stack.peek()) && priority.get(elem) <= priority.get(stack.peek())) {
                    // 并添加到后缀表达式末尾
                    postfix.add(stack.pop());
                }
                // 将扫描到的运算符压入栈
                stack.push(elem);
                // 遇到右圆括号
            } else if (")".equals(elem)) {
                // 将遇到左圆括号之前的所有运算符弹出栈, 并追加到后缀表达式
                while (!"(".equals(stack.peek())) {
                    postfix.add(stack.pop());
                }
                // 再丢弃左圆括号
                stack.pop();
            }
        }
        // 将栈中剩余的加入后缀表达式
        while (!stack.isEmpty()) {
            postfix.add(stack.pop());
        }
        return postfix.toArray(new String[0]);
    }

    /**
     * 判断字符串是否为数字
     *
     * @param str 字符串
     * @return boolean 判定结果
     */
    private static boolean isNumeric(String str) {
        //Pattern pattern = Pattern.compile("^-?[0-9]+"); //这个也行
        // ^-?\d+(\.\d+)?$
        Pattern pattern = Pattern.compile("\\d+");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    /**
     * 判断字符串是否为操作符
     *
     * @param str 字符串
     * @return boolean 判定结果
     */
    private static boolean isOperator(String str) {
        return "+".equals(str) || "-".equals(str) || "*".equals(str) || "/".equals(str);
    }

    /**
     * 计算后缀表达式
     *
     * @param postfix 后缀表达式
     * @return double 计算结果
     */
    private static double postfixCalculate(String[] postfix) {
        LinkedList<Double> stack = new LinkedList<>();
        // 遍历后缀表达式
        for (String elem : postfix) {
            // 遇到数字就压入栈
            if (isNumeric(elem)) {
                stack.push(Double.valueOf(elem));
                // 遇到运算符
            } else if (isOperator(elem)) {
                // 弹出两个栈元素
                double a = stack.pop();
                double b = stack.pop();
                // 将计算结果压入栈
                stack.push(calculate(a, b, elem));
            }
        }
        return stack.pop();
    }

    /**
     * 根据两个数和运算符计算结果
     *
     * @param a        第一个数
     * @param b        第二个数
     * @param operator 操作符
     * @return double 计算结果
     */
    private static double calculate(double a, double b, String operator) {
        // 注意a和b的位置
        switch (operator) {
            case "+":
                return b + a;
            case "-":
                return b - a;
            case "*":
                return b * a;
            case "/":
                return b / a;
            default:
                return 0.0d;
        }
    }
}