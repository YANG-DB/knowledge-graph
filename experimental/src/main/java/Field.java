import java.util.List;

/**
 * Field
 * @param dashed_name
 * @param description
 * @param example
 * @param flat_name
 * @param level
 * @param name
 * @param type
 * @param normalize
 */
record Field(
        String dashed_name,
        String description,
        String example,
        String flat_name,
        String level,
        String name,
        String type,
        List normalize) {
}
