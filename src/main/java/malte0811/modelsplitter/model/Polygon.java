package malte0811.modelsplitter.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import malte0811.modelsplitter.math.EpsilonMath;
import malte0811.modelsplitter.math.Plane;
import malte0811.modelsplitter.util.CyclicListWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Polygon<Texture>
{
    private static final EpsilonMath EPS_MATH = new EpsilonMath(1e-5);
    private final List<Vertex> points;
    private final Texture texture;

    public Polygon(List<Vertex> points, Texture texture)
    {
        this.points = ImmutableList.copyOf(points);
        this.texture = texture;
    }

    public Polygon(Vertex first, List<Vertex> inner, Vertex last, Texture texture)
    {
        List<Vertex> points = new ArrayList<>();
        if(!EPS_MATH.areSame(first.getPosition(), inner.get(0).getPosition()))
        {
            points.add(first);
        }
        points.addAll(inner);
        if(!EPS_MATH.areSame(inner.get(inner.size()-1).getPosition(), last.getPosition()))
        {
            points.add(last);
        }
        this.points = ImmutableList.copyOf(points);
        this.texture = texture;
    }

    public List<Vertex> getPoints()
    {
        return points;
    }

    public Texture getTexture()
    {
        return texture;
    }

    public Map<EpsilonMath.Sign, Polygon<Texture>> splitAlong(Plane p)
    {
        List<EpsilonMath.Sign> signs = new ArrayList<>(points.size());
        for(final Vertex point : points)
        {
            final double product = p.normal.dotProduct(point.getPosition())-p.dotProduct;
            signs.add(EPS_MATH.sign(product));
        }
        int firstSignStart = 0;
        final EpsilonMath.Sign otherSign;
        final EpsilonMath.Sign firstSign;
        {
            final EpsilonMath.Sign zeroSign = signs.get(0);
            while(firstSignStart < points.size())
            {
                final EpsilonMath.Sign signHere = signs.get(firstSignStart);
                if(zeroSign!=signHere&&signHere!=EpsilonMath.Sign.ZERO)
                {
                    break;
                }
                ++firstSignStart;
            }
            // If all points have the same sign we don't need to do any splitting
            if(firstSignStart >= points.size())
            {
                return ImmutableMap.of(zeroSign, this);
            }
            firstSign = signs.get(firstSignStart);
            otherSign = firstSign.invert();
            // Same if we only have zero and one sign
            if(!signs.contains(otherSign))
            {
                return ImmutableMap.of(firstSign, this);
            }
        }
        CyclicListWrapper<EpsilonMath.Sign> cyclicSigns = new CyclicListWrapper<>(signs);
        CyclicListWrapper<Vertex> cyclicPoints = new CyclicListWrapper<>(points);
        int otherSignStart = firstSignStart;
        while(cyclicSigns.get(otherSignStart)!=otherSign)
        {
            ++otherSignStart;
        }
        List<Vertex> firstInnerPoints = cyclicPoints.sublist(firstSignStart, otherSignStart);
        List<Vertex> otherInnerPoints = cyclicPoints.sublist(otherSignStart, firstSignStart);
        Vertex firstNewPoint = intersect(cyclicPoints.get(firstSignStart-1), cyclicPoints.get(firstSignStart), p);
        Vertex otherNewPoint = intersect(cyclicPoints.get(otherSignStart-1), cyclicPoints.get(otherSignStart), p);
        return ImmutableMap.of(
                firstSign, new Polygon<>(firstNewPoint, firstInnerPoints, otherNewPoint, getTexture()),
                otherSign, new Polygon<>(otherNewPoint, otherInnerPoints, firstNewPoint, getTexture())
        );
    }

    private Vertex intersect(Vertex a, Vertex b, Plane p)
    {
        final double productA = a.getPosition().dotProduct(p.normal);
        final double productB = b.getPosition().dotProduct(p.normal);
        double lambda = (p.dotProduct-productB)/(productA-productB);
        return Vertex.interpolate(a, b, lambda);
    }

    public Polygon<Texture> translate(int axis, double amount)
    {
        List<Vertex> translatedVertices = new ArrayList<>(points.size());
        for(Vertex v : points)
        {
            translatedVertices.add(v.translate(axis, amount));
        }
        return new Polygon<>(translatedVertices, texture);
    }

    public List<Polygon<Texture>> quadify()
    {
        List<Polygon<Texture>> quads = new ArrayList<>();
        int secondVertex = 1;
        while(secondVertex+2 < points.size())
        {
            quads.add(new Polygon<>(ImmutableList.of(
                    points.get(0),
                    points.get(secondVertex),
                    points.get(secondVertex+1),
                    points.get(secondVertex+2)
            ), getTexture()));
            secondVertex += 3;
        }
        if(secondVertex+1 < points.size())
        {
            quads.add(new Polygon<>(ImmutableList.of(
                    points.get(0),
                    points.get(secondVertex),
                    points.get(secondVertex+1),
                    points.get(secondVertex+1)
            ), getTexture()));
        }
        return quads;
    }
}
